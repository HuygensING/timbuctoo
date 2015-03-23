package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

public enum FieldType {
  ADMINISTRATIVE, //
  REGULAR {
    @Override
    public String propertyName(Class<? extends Entity> type, String fieldName) {

      return String.format("%s:%s", TypeNames.getInternalName(type), fieldName);
    }
  }, //
  VIRTUAL;

  public String propertyName(Class<? extends Entity> type, String fieldName) {
    return fieldName;
  }
}
