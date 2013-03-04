package nl.knaw.huygens.repository.importer.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.generic.StorageFactory;
import nl.knaw.huygens.repository.util.Configuration;

public class PersonImporter {

  private String connectionString;
  private String userName;
  private String password;
  private String query;
  private Map<String, List<String>> objectMapping;

  public static void main(String[] args) throws SQLException, ConfigurationException, IOException {
    Configuration conf = new Configuration("config.xml");
    Hub hub = new Hub();
    DocumentTypeRegister docTypeRegistry = new DocumentTypeRegister();

    StorageConfiguration storageConfiguration = new StorageConfiguration(conf);
    Storage storage = StorageFactory.getInstance(storageConfiguration, docTypeRegistry);
    StorageManager storageManager = new StorageManager(storageConfiguration, storage, hub, docTypeRegistry);
    storageManager.getStorage().empty();

    PersonImporter importer = new PersonImporter();

    importer.importData("resources/DWCPersonMapping.properties", storageManager);
    importer.importData("resources/RAAPersonMapping.properties", storageManager);

    storageManager.ensureIndices();
  }

  public void importData(String configFile, StorageManager storageManager) throws SQLException, IOException {
    this.readMapping(configFile);
    this.importPersons(this.connectionString, this.userName, this.password, this.query, this.objectMapping, storageManager);
  }

  private void importPersons(String mySQLURL, String mySQLUser, String mySQLPwd, String query, Map<String, List<String>> propertyMapping, StorageManager storageManager)
      throws SQLException, IOException {
    PersonResultSetConverter mySQLConverter = new PersonResultSetConverter(propertyMapping);

    SQLImporter mySQLImporter = new SQLImporter(mySQLURL, mySQLUser, mySQLPwd);
    List<Person> persons = mySQLImporter.executeQuery(query, mySQLConverter);

    for (Person person : persons) {
      System.out.println(person.getDescription());
      storageManager.addDocument(person, Person.class);
    }

    System.out.println("persons.size(): " + persons.size());
  }

  private void readMapping(String filePath) throws IOException {
    Properties mapping = new Properties();
    mapping.load(new FileInputStream(filePath));

    this.objectMapping = new HashMap<String, List<String>>();
    for (Entry<Object, Object> entry : mapping.entrySet()) {
      if ("connectionString".equals(entry.getKey())) {
        this.connectionString = (String) entry.getValue();
      } else if ("userName".equals(entry.getKey())) {
        this.userName = (String) entry.getValue();
      } else if ("password".equals(entry.getKey())) {
        this.password = (String) entry.getValue();
      } else if ("query".equals(entry.getKey())) {
        this.query = (String) entry.getValue();
      } else {
        String key = (String) entry.getKey();
        List<String> value = getEntryValue((String) entry.getValue());
        this.objectMapping.put(key, value);
      }
    }
  }

  private List<String> getEntryValue(String value) {
    List<String> values = new ArrayList<String>();
    String[] valueParts = value.split(",");

    for (String valuePart : valueParts) {
      values.add(valuePart.trim());
    }

    return values;
  }
}
