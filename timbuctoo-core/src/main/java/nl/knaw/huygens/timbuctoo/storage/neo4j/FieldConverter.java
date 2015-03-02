package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.Node;

public interface FieldConverter {

  public abstract void setContainingType(Class<? extends Entity> containingType);

  public abstract void setField(Field field);

  public abstract void addValueToNode(Node node, Entity entity) throws ConversionException;

  /**
   * Extracts the value from the node and converts it so it can be added to the entity.
   * @param entity the entity to add the values to
   * @param node the node to retrieve the values from
   * @throws ConversionException when the value in the node could not be added to the entity.
   */
  public abstract void addValueToEntity(Entity entity, Node node) throws ConversionException;

  public abstract void setFieldType(FieldType fieldType);

  public abstract void setName(String fieldName);

}