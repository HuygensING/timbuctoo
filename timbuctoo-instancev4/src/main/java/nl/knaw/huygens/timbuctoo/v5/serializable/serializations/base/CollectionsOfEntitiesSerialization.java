package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import nl.knaw.huygens.timbuctoo.v5.serializable.dto.SerializableList;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableResult;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class CollectionsOfEntitiesSerialization implements Serialization {

  protected Map<String, Map<String, Map<String, Object>>> allEntities;

  public CollectionsOfEntitiesSerialization() {
    allEntities = new HashMap<>();
  }

  @Override
  public void serialize(SerializableResult serializableResult) throws IOException {
    convert(allEntities, serializableResult.getData());
  }

  private Object convert(Map<String, Map<String, Map<String, Object>>> result, Object value) {
    if (value instanceof Map) {
      Map<String, Object> data = (Map) value;
      if (data.containsKey("@id") && data.containsKey("@type")) {
        Map<String, Object> target = result
          .computeIfAbsent(data.get("@type") + "", key -> new HashMap<>())
          .computeIfAbsent(data.get("@id") + "", key -> new HashMap<>());
        for (Map.Entry<String, Object> entry : data.entrySet()) {
          Object converted = convert(result, entry.getValue());
          target.put(entry.getKey(), converted);
        }
        return target;
      } else {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
          convert(result, entry.getValue());
        }
        return data;
      }
    } else if (value instanceof SerializableList) {
      java.util.List items = ((SerializableList) value).getItems();
      ArrayList list = new ArrayList(items.size());
      for (Object item : items) {
        list.add(convert(result, item));
      }
      return list;
    } else {
      return value;
    }
  }

}
