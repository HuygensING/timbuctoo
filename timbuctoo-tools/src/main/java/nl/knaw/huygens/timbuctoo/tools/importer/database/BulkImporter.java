package nl.knaw.huygens.timbuctoo.tools.importer.database;

import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.IndexService;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCPlace;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCScientist;
import nl.knaw.huygens.timbuctoo.model.raa.RAACivilServant;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

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
        RelationManager relationManager = new RelationManager(registry, storageManager);
        new AtlantischeGidsImporter(registry, relationManager, storageManager, "../AtlantischeGids/work/").importAll();
      } else {
        String resourceDir = "src/main/resources/";
        importer.importData(resourceDir + "DWCPlaceMapping.properties", DWCPlace.class);
        importer.importData(resourceDir + "DWCScientistMapping.properties", DWCScientist.class);
        importer.importData(resourceDir + "RAACivilServantMapping.properties", RAACivilServant.class);
        CKCCPersonImporter csvImporter = new CKCCPersonImporter(storageManager);
        csvImporter.handleFile(resourceDir + "testdata/ckcc-persons.txt", 9, false);
      }

      // Signal we're done
      DefaultImporter.sendEndOfDataMessage(broker);

      long time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n%n", time);

      DefaultImporter.waitForCompletion(thread, 5 * 60 * 1000);

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

}
