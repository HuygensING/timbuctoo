package nl.knaw.huygens.repository.importer.database;

import nl.knaw.huygens.repository.BasicInjectionModule;
import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.dwcbia.DWCScientist;
import nl.knaw.huygens.repository.model.raa.RAACivilServant;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class PersonImporter {

  public static void main(String[] args) throws Exception {
    Injector injector = Guice.createInjector(new BasicInjectionModule("config.xml"));
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    storageManager.getStorage().empty();

    GenericImporter importer = new GenericImporter();

    importer.importData("resources/DWCScientistMapping.properties", storageManager, DWCScientist.class);
    importer.importData("resources/RAACivilServantMapping.properties", storageManager, RAACivilServant.class);

    storageManager.ensureIndices();
  }

}