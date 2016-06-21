package nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ImportPropertyDescriptions {
  private HashMap<Integer, ImportPropertyDescription> propertyDescs = new HashMap<>();
  private List<ImportPropertyDescription> ordered = new ArrayList<>();

  public ImportPropertyDescriptions() {
  }

  public Optional<ImportPropertyDescription> get(int id) {
    if (propertyDescs.containsKey(id)) {
      return Optional.of(propertyDescs.get(id));
    } else {
      return Optional.empty();
    }
  }

  public ImportPropertyDescription getByOrder(int order) {
    return ordered.get(order);
  }


  public ImportPropertyDescription getOrCreate(int id) {
    if (propertyDescs.containsKey(id)) {
      return propertyDescs.get(id);
    } else {
      ImportPropertyDescription desc = new ImportPropertyDescription(id, ordered.size());
      ordered.add(desc);
      propertyDescs.put(id, desc);
      return desc;
    }
  }

  public int getPropertyCount() {
    return ordered.size();
  }
}
