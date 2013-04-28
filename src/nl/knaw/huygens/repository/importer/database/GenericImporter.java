package nl.knaw.huygens.repository.importer.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.util.Progress;

public class GenericImporter {
  private String connectionString;
  private String userName;
  private String password;
  private String query;
  private Map<String, List<String>> objectMapping;

  public <T extends Document> void importData(String configFile, StorageManager storageManager, Class<T> type) throws Exception {
    System.out.printf("%n=== Import documents of type '%s'%n", type.getSimpleName());

    readMapping(configFile);
    GenericResultSetConverter<T> converter = new GenericResultSetConverter<T>(objectMapping, type);
    SQLImporter importer = new SQLImporter(connectionString, userName, password);
    List<T> objects = importer.executeQuery(query, converter);

    Progress progress = new Progress();
    for (T object : objects) {
      progress.step();
      // System.out.println(object.getDescription());
      storageManager.addDocument(type, object);
    }
    progress.done();
  }

  private void readMapping(String filePath) throws IOException {
    Properties mapping = new Properties();
    mapping.load(new FileInputStream(filePath));

    objectMapping = new HashMap<String, List<String>>();
    for (Entry<Object, Object> entry : mapping.entrySet()) {
      if ("connectionString".equals(entry.getKey())) {
        connectionString = (String) entry.getValue();
      } else if ("userName".equals(entry.getKey())) {
        userName = (String) entry.getValue();
      } else if ("password".equals(entry.getKey())) {
        password = (String) entry.getValue();
      } else if ("query".equals(entry.getKey())) {
        query = (String) entry.getValue();
      } else {
        String key = (String) entry.getKey();
        List<String> value = getEntryValue((String) entry.getValue());
        objectMapping.put(key, value);
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
