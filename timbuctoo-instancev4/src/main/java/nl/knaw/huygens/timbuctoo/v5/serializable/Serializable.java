package nl.knaw.huygens.timbuctoo.v5.serializable;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;

import java.util.Map;

public class Serializable {
  private final Map<String, Object> data;
  private final TypeNameStore typeNameStore;

  public Serializable(Map<String, Object> data, TypeNameStore typeNameStore) {
    this.data = data;
    this.typeNameStore = typeNameStore;
  }

  public Map<String, Object> getData() {
    return data;
  }
}
