package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;

public class PropertyBusinessRules {

  public boolean isAdministrativeProperty(Field field) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isVirtualProperty(Field field) {
    // TODO Auto-generated method stub
    return false;
  }

  public FieldType getFieldType(Class<? extends Entity> containingType, Field field) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getFieldName(Class<? extends Entity> containingType, Field field) {
    // TODO Auto-generated method stub
    return null;
  }

}
