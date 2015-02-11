package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.SystemEntity;

public abstract class FieldWrapper {

  public abstract void setField(Field field);

  public abstract void setContainingEntity(SystemEntity entity);

}
