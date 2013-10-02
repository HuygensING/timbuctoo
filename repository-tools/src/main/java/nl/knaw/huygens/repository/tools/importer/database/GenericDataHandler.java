package nl.knaw.huygens.repository.tools.importer.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import nl.knaw.huygens.repository.model.Entity;

public abstract class GenericDataHandler {

  protected static final String QUERY_LABEL = "query";
  protected static final String PASSWORD_LABEL = "password";
  protected static final String USER_NAME_LABEL = "userName";
  protected static final String CONNECTION_STRING_LABEL = "connectionString";

  protected String connectionString;
  protected String userName;
  protected String password;
  protected String query;

  private Map<String, List<String>> objectMapping;
  public <T extends Entity> void importData(String configFile, Class<T> type) throws Exception {
    System.out.printf("%n=== Import documents of type '%s'%n", type.getSimpleName());

    readMapping(configFile);
    GenericResultSetConverter<T> converter = new GenericResultSetConverter<T>(objectMapping, type);
    SQLImporter importer = new SQLImporter(connectionString, userName, password);
    List<T> objects = importer.executeQuery(query, converter);

    save(type, objects);
  }

  protected void readMapping(String filePath) throws IOException {
    Properties mapping = new Properties();
    mapping.load(new FileInputStream(filePath));

    objectMapping = new HashMap<String, List<String>>();
    for (Entry<Object, Object> entry : mapping.entrySet()) {
      if (CONNECTION_STRING_LABEL.equals(entry.getKey())) {
        connectionString = (String) entry.getValue();
      } else if (USER_NAME_LABEL.equals(entry.getKey())) {
        userName = (String) entry.getValue();
      } else if (PASSWORD_LABEL.equals(entry.getKey())) {
        password = (String) entry.getValue();
      } else if (QUERY_LABEL.equals(entry.getKey())) {
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

  protected abstract <T extends Entity> void save(Class<T> type, List<T> objects) throws IOException;

}