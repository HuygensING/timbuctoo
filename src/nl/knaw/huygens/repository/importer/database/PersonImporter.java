package nl.knaw.huygens.repository.importer.database;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.apache.commons.configuration.ConfigurationException;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.dwcbia.DWCPerson;
import nl.knaw.huygens.repository.model.raa.RAAPerson;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.generic.StorageFactory;
import nl.knaw.huygens.repository.util.Configuration;

public class PersonImporter {

  public static void main(String[] args) throws SQLException, ConfigurationException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
    Configuration conf = new Configuration("config.xml");
    Hub hub = new Hub();
    DocumentTypeRegister docTypeRegistry = new DocumentTypeRegister();
    docTypeRegistry.registerPackageFromClass(DWCPerson.class);
    docTypeRegistry.registerPackageFromClass(RAAPerson.class);

    StorageConfiguration storageConfiguration = new StorageConfiguration(conf);
    Storage storage = StorageFactory.getInstance(storageConfiguration, docTypeRegistry);
    StorageManager storageManager = new StorageManager(storageConfiguration, storage, hub, docTypeRegistry);
    storageManager.getStorage().empty();

    GenericImporter importer = new GenericImporter();

    importer.importData("resources/DWCPersonMapping.properties", storageManager, DWCPerson.class);
    //importer.importData("resources/RAAPersonMapping.properties", storageManager, RAAPerson.class);

    storageManager.ensureIndices();
  }
}