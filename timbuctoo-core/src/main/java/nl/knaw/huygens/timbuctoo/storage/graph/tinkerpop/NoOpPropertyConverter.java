package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType;

import com.tinkerpop.blueprints.Vertex;

public class NoOpPropertyConverter implements PropertyConverter {

  @Override
  public void setField(Field field) {}

  @Override
  public void setContainingType(Class<? extends Entity> type) {}

  @Override
  public void setFieldType(FieldType fieldType) {}

  @Override
  public void setName(String fieldName) {}

  @Override
  public void setPropertyOfVertex(Vertex vertex, Entity entity) throws ConversionException {}

  @Override
  public void addValueToEntity(Entity entity, Vertex vertex) throws ConversionException {}

}
