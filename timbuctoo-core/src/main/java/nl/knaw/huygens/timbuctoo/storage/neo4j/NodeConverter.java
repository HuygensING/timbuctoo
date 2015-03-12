package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.Node;

public interface NodeConverter<T extends Entity> extends PropertyContainerConverter<Node, T> {

  @Override
  void addValuesToPropertyContainer(Node node, T entity) throws ConversionException;

  @Override
  void addValuesToEntity(T entity, Node node) throws ConversionException;

  /**
   * Updates the non administrative properties of the node.
   * @param node the node to update
   * @param entity the entity that contains the data.
   * @throws ConversionException when the fieldConverter throws one.
   */
  @Override
  void updatePropertyContainer(Node node, T entity) throws ConversionException;

  /**
   * Updates the modified and revision properties of the node.
   * @param node the node to update
   * @param entity the entity that contains the data to update.
   * @throws ConversionException when one of the FieldConverters throws one 
   */
  @Override
  void updateModifiedAndRev(Node node, T entity) throws ConversionException;

}