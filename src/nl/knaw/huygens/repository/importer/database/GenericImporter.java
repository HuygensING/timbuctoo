package nl.knaw.huygens.repository.importer.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;

public class GenericImporter {
  private String connectionString;
  private String userName;
  private String password;
  private String query;
  private Map<String, List<String>> objectMapping;

  public <T extends Document> void importData(String configFile, StorageManager storageManager, Class<T> type) throws SQLException, IOException, InstantiationException, IllegalAccessException,
      NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
    this.readMapping(configFile);
    this.importPersons(this.connectionString, this.userName, this.password, this.query, this.objectMapping, storageManager, type);
  }

  private <T extends Document> void importPersons(String mySQLURL, String mySQLUser, String mySQLPwd, String query, Map<String, List<String>> propertyMapping, StorageManager storageManager,
      Class<T> type) throws SQLException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
    GenericResultSetConverter<T> resultSetCoverter = new GenericResultSetConverter<T>(propertyMapping, type);

    SQLImporter importer = new SQLImporter(mySQLURL, mySQLUser, mySQLPwd);
    List<T> objects = importer.executeQuery(query, resultSetCoverter);

    for (T object : objects) {
      System.out.println(object.getDescription());
      storageManager.addDocument(object, type);
    }

    System.out.println("persons.size(): " + objects.size());
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
    for (String valuePart : value.split(",")) {
      values.add(valuePart.trim());
    }
    return values;
  }

}
