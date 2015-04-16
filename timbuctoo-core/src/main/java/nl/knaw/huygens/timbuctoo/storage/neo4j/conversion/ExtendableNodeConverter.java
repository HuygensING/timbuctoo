package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.neo4j.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.neo4j.NodeConverter;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

class ExtendableNodeConverter<T extends Entity> extends AbstractExtendablePropertyContainerConverter<Node, T> implements NodeConverter<T>, ExtendablePropertyContainerConverter<Node, T> {

  public ExtendableNodeConverter(Class<T> type) {
    this(type, new EntityInstantiator());
  }

  public ExtendableNodeConverter(Class<T> type, EntityInstantiator entityInstantiator) {
    super(type, entityInstantiator);

  }

  @Override
  protected void executeCustomSerializationActions(Node propertyContainer, T entity) {
    propertyContainer.addLabel(DynamicLabel.label(TypeNames.getInternalName(type)));
  }

  @Override
  protected void executeCustomDeserializationActions(T entity, Node propertyContainer) {
    // nothing to do
  }

  @Override
  public <U extends T> U convertToSubType(Class<U> type, Node node) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }
}
