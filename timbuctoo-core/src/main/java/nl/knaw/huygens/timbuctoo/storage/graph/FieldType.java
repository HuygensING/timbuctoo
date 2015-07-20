package nl.knaw.huygens.timbuctoo.storage.graph;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

public enum FieldType {
  ADMINISTRATIVE, //
  REGULAR {
    @Override
    public String completePropertyName(Class<? extends Entity> type, String propertyName) {

      return String.format("%s:%s", TypeNames.getInternalName(type), propertyName);
    }
  }, //
  VIRTUAL;

  public String completePropertyName(Class<? extends Entity> type, String propertyName) {
    return propertyName;
  }
}
