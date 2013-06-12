package nl.knaw.huygens.repository.importer.database;

import javax.jms.JMSException;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.index.IndexManager;
import nl.knaw.huygens.repository.index.IndexService;
import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.messages.Broker;
import nl.knaw.huygens.repository.messages.Producer;
import nl.knaw.huygens.repository.model.dwcbia.DWCPlace;
import nl.knaw.huygens.repository.model.dwcbia.DWCScientist;
import nl.knaw.huygens.repository.model.raa.RAACivilServant;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Performs bulk import of test data.
 * The data is both stored (Mongo) and indexed (Solr).
 */
public class BulkImporter {

  public static void main(String[] args) throws Exception {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new BasicInjectionModule(config));

    StorageManager storageManager = null;
    IndexManager indexManager = null;

    try {
      storageManager = injector.getInstance(StorageManager.class);
      storageManager.getStorage().empty();

      indexManager = injector.getInstance(IndexManager.class);
      indexManager.deleteAllDocuments();

      IndexService service = injector.getInstance(IndexService.class);
      Thread thread = new Thread(service);
      thread.start();

      GenericImporter importer = new GenericImporter(storageManager);

      long start = System.currentTimeMillis();
      importer.importData("resources/DWCPlaceMapping.properties", DWCPlace.class);
      importer.importData("resources/DWCScientistMapping.properties", DWCScientist.class);
      importer.importData("resources/RAACivilServantMapping.properties", RAACivilServant.class);
      CKCCPersonImporter csvImporter = new CKCCPersonImporter(storageManager);
      csvImporter.handleFile("testdata/ckcc-persons.txt", 9, false);

      storageManager.ensureIndices();

      Broker broker = injector.getInstance(Broker.class);
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
    }
  }

  private static void sendEndOfDataMessage(Broker broker) throws JMSException {
    Producer producer = broker.newProducer(Broker.INDEX_QUEUE, BulkImporter.class.getSimpleName());
    producer.send(Broker.INDEX_END, "", "");
    producer.close();
  }

  private static void waitForCompletion(Thread thread, long patience) throws InterruptedException {
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
