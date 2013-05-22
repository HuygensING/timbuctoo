package nl.knaw.huygens.repository.importer.database;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.managers.IndexManager;
import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.dwcbia.DWCPlace;
import nl.knaw.huygens.repository.model.dwcbia.DWCScientist;
import nl.knaw.huygens.repository.model.raa.RAACivilServant;
import nl.knaw.huygens.repository.pubsub.Hub;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class BulkImporter {

  public static void main(String[] args) throws Exception {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new BasicInjectionModule(config));

    StorageManager storageManager = injector.getInstance(StorageManager.class);
    storageManager.getStorage().empty();

    IndexManager indexManager = injector.getInstance(IndexManager.class);
    indexManager.clearIndexes();
    // TODO change to messaging implementation
    injector.getInstance(Hub.class).subscribe(indexManager);

    GenericImporter importer = new GenericImporter();

    long start = System.currentTimeMillis();
    importer.importData("resources/DWCPlaceMapping.properties", storageManager, DWCPlace.class);
    importer.importData("resources/DWCScientistMapping.properties", storageManager, DWCScientist.class);
    importer.importData("resources/RAACivilServantMapping.properties", storageManager, RAACivilServant.class);
    CKCCPersonImporter csvImporter = new CKCCPersonImporter(storageManager);
    csvImporter.handleFile("testdata/ckcc-persons.txt", 9, false);

    storageManager.ensureIndices();
    indexManager.close();

    long time = (System.currentTimeMillis() - start) / 1000;
    System.out.printf("%n=== Import took %d seconds", time);
  }

}
