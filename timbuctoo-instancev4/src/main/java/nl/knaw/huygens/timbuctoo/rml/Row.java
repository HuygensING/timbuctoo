package nl.knaw.huygens.timbuctoo.rml;

import java.util.Map;

public class Row {
  private final Map<String, Object> data;

  public Row(Map<String, Object> data) {
    this.data = data;
  }

  public Object get(String key) {
    return data.get(key);
  }
}
