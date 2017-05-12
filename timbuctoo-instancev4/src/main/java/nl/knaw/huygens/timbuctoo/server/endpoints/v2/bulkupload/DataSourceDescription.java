package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import java.util.Map;

public class DataSourceDescription {
  private final String collection;
  private final Map<String, String> customFields;

  public String getCollection() {
    return collection;
  }

  public Map<String, String> getCustomFields() {
    return customFields;
  }

  public DataSourceDescription(String collection, Map<String, String> customFields) {
    this.collection = collection;

    this.customFields = customFields;
  }
}
