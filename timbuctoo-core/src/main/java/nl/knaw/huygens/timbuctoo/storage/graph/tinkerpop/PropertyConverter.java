package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType;

public interface PropertyConverter {

  void setField(Field field);

  void setContainingType(Class<? extends Entity> type);

  void setFieldType(FieldType fieldType);

  void setName(String fieldName);
}
