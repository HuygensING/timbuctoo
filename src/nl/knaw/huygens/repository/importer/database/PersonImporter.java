package nl.knaw.huygens.repository.importer.database;

import com.google.inject.Guice;
import com.google.inject.Injector;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.dwcbia.DWCPerson;
import nl.knaw.huygens.repository.model.raa.RAAPerson;
import nl.knaw.huygens.repository.modules.RepositoryBasicModule;

public class PersonImporter {

  public static void main(String[] args) throws Exception {
    Injector injector = Guice.createInjector(new RepositoryBasicModule("config.xml"));
    StorageManager storageManager = injector.getInstance(StorageManager.class);
    storageManager.getStorage().empty();

    GenericImporter importer = new GenericImporter();

    importer.importData("resources/DWCPersonMapping.properties", storageManager, DWCPerson.class);
    importer.importData("resources/RAAPersonMapping.properties", storageManager, RAAPerson.class);

    storageManager.ensureIndices();
  }
}