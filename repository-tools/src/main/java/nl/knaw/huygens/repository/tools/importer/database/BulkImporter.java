package nl.knaw.huygens.repository.tools.importer.database;

import javax.jms.JMSException;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.index.IndexManager;
import nl.knaw.huygens.repository.index.IndexService;
import nl.knaw.huygens.repository.messages.ActionType;
import nl.knaw.huygens.repository.messages.Broker;
import nl.knaw.huygens.repository.messages.Producer;
import nl.knaw.huygens.repository.model.dwcbia.DWCPlace;
import nl.knaw.huygens.repository.model.dwcbia.DWCScientist;
import nl.knaw.huygens.repository.model.raa.RAACivilServant;
import nl.knaw.huygens.repository.storage.StorageManager;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Performs bulk import of test data, which is stored and indexed.
 * <br/>
 * There are three infrastructure components involved:<ul>
 * <li>Mongo for storage of the documents;</li>
 * <li>Solr for indexing of the documents;</li>
 * <li>ActiveMQ for communicating that documents have been stored
 * which need to be indexed.</li>
 * </ul>
 * Mongo depends on ActiveMQ, Solr depend on Mongo and ActiveMQ.
 * These dependencies imply the order in which the components must be
 * opened and closed.
 */
public class BulkImporter {

  public static boolean ATLG = false;

  public static void main(String[] args) throws Exception {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new BasicInjectionModule(config));

    Broker broker = null;
    StorageManager storageManager = null;
    IndexManager indexManager = null;

    try {
      broker = injector.getInstance(Broker.class);
      broker.start();

      storageManager = injector.getInstance(StorageManager.class);
      storageManager.clear();

      indexManager = injector.getInstance(IndexManager.class);
      indexManager.deleteAllDocuments();

      IndexService service = injector.getInstance(IndexService.class);
      Thread thread = new Thread(service);
      thread.start();

      GenericImporter importer = new GenericImporter(storageManager);

      long start = System.currentTimeMillis();
      if (ATLG) {
        LanguageImporter languageImporter = new LanguageImporter(storageManager);
        languageImporter.handleFile("testdata/iso-639-2-language-codes.txt", 5, false);
        DocTypeRegistry registry = injector.getInstance(DocTypeRegistry.class);
        DataPoster poster = new LocalDataPoster(storageManager);
        new AtlantischeGidsImporter(registry, poster, "../AtlantischeGids/work/").importAll();
      } else {
        String resourceDir = "src/main/resources/";
        importer.importData(resourceDir + "DWCPlaceMapping.properties", DWCPlace.class);
        importer.importData(resourceDir + "DWCScientistMapping.properties", DWCScientist.class);
        importer.importData(resourceDir + "RAACivilServantMapping.properties", RAACivilServant.class);
        CKCCPersonImporter csvImporter = new CKCCPersonImporter(storageManager);
        csvImporter.handleFile(resourceDir + "testdata/ckcc-persons.txt", 9, false);
      }

      storageManager.ensureIndices();

      // Signal we're done
      sendEndOfDataMessage(broker);

      long time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n%n", time);

      waitForCompletion(thread, 5 * 60 * 1000);

      time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n", time);

    } finally {
      // Close resources
      if (indexManager != null) {
        indexManager.close();
      }
      if (storageManager != null) {
        storageManager.close();
      }
      if (broker != null) {
        broker.close();
      }
    }
  }

  public static void sendEndOfDataMessage(Broker broker) throws JMSException {
    Producer producer = broker.newProducer(Broker.INDEX_QUEUE, "ImporterProducer");
    producer.send(ActionType.INDEX_END, "", "");
    producer.close();
  }

  public static void waitForCompletion(Thread thread, long patience) throws InterruptedException {
    long targetTime = System.currentTimeMillis() + patience;
    while (thread.isAlive()) {
      System.out.println("... indexing");
      thread.join(2500);
      if (System.currentTimeMillis() > targetTime && thread.isAlive()) {
        System.out.println("... tired of waiting!");
        thread.interrupt();
        thread.join();
      }
    }
  }

}
