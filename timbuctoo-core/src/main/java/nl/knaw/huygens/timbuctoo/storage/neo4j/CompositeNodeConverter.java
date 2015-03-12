package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.Node;

public class CompositeNodeConverter<T extends Entity> implements NodeConverter<T> {

  private List<NodeConverter<? super T>> nodeConverters;

  public CompositeNodeConverter(List<NodeConverter<? super T>> nodeConverters) {
    this.nodeConverters = nodeConverters;
  }

  @Override
  public void addValuesToPropertyContainer(Node node, T entity) throws ConversionException {
    for (NodeConverter<? super T> converter : nodeConverters) {
      converter.addValuesToPropertyContainer(node, entity);
    }
  }

  @Override
  public void addValuesToEntity(T entity, Node node) throws ConversionException {
    for (NodeConverter<? super T> converter : nodeConverters) {
      converter.addValuesToEntity(entity, node);
    }
  }

  @Override
  public void updatePropertyContainer(Node node, T entity) throws ConversionException {
    for (NodeConverter<? super T> converter : nodeConverters) {
      converter.updatePropertyContainer(node, entity);
    }
  }

  @Override
  public void updateModifiedAndRev(Node node, T entity) throws ConversionException {
    for (NodeConverter<? super T> converter : nodeConverters) {
      converter.updateModifiedAndRev(node, entity);
    }
  }

  @Override
  public Object getPropertyValue(Node node, String fieldName) throws ConversionException {
    throw new UnsupportedOperationException("Will not be implemented");
  }

  public List<NodeConverter<? super T>> getNodeConverters() {
    return nodeConverters;
  }

}
