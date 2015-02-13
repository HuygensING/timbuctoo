package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.Entity;

public enum FieldType {
  ADMINISTRATIVE, REGULAR, VIRTUAL;

  public String propertyName(Class<? extends Entity> type, String fieldName) {
    // TODO Auto-generated method stub
    return null;
  }
}
