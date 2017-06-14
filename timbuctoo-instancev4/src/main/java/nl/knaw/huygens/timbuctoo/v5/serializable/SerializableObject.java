package nl.knaw.huygens.timbuctoo.v5.serializable;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SerializableObject implements Serializable {
  private final LinkedHashMap<String, Serializable> data;
  private final String uri;
  private final TypeNameStore typeNameStore;

  public SerializableObject(LinkedHashMap<String, Serializable> data, String uri, TypeNameStore typeNameStore) {
    this.data = data;
    this.uri = uri;
    this.typeNameStore = typeNameStore;
  }

  @Override
  public void serialize(Serialization serialization) throws IOException {
    serialization.onStartEntity(uri);
    for (Map.Entry<String, Serializable> entry : data.entrySet()) {
      serialization.onProperty(entry.getKey());
      entry.getValue().serialize(serialization);
    }
    serialization.onCloseEntity(uri);
  }

  @Override
  public void generateToC(ResultToC siblingEntity) {
    for (Map.Entry<String, Serializable> entry : data.entrySet()) {
      entry.getValue().generateToC(siblingEntity.getField(entry.getKey()));
    }
  }

  public void performSerialization(Serialization serialization) throws IOException {
    serialization.initialize(() -> {
      ResultToC result = new ResultToC();
      this.generateToC(result);
      return result;
    }, typeNameStore);
    this.serialize(serialization);
    serialization.finish();

  }
}
