package nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.RelationshipConverter;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

class ExtendableRelationshipConverter<T extends Relation> extends AbstractExtendablePropertyContainerConverter<Relationship, T> implements RelationshipConverter<T>,
    ExtendablePropertyContainerConverter<Relationship, T> {

  private final TypeRegistry typeRegistry;

  public ExtendableRelationshipConverter(Class<T> type, TypeRegistry typeRegistry, EntityInstantiator entityInstantiatorMock) {
    super(type, entityInstantiatorMock);
    this.typeRegistry = typeRegistry;
  }

  public ExtendableRelationshipConverter(Class<T> type, TypeRegistry typeRegistry) {
    this(type, typeRegistry, new EntityInstantiator());
  }

  @Override
  protected void executeCustomSerializationActions(Relationship propertyContainer, T entity) {}

  @Override
  protected void executeCustomDeserializationActions(T entity, Relationship propertyContainer) {
    Node startNode = propertyContainer.getStartNode();
    if (startNode.hasProperty(ID_PROPERTY_NAME)) {
      entity.setSourceId((String) startNode.getProperty(ID_PROPERTY_NAME));
    }

    entity.setSourceType(getPrimitiveType(startNode));

    Node endNode = propertyContainer.getEndNode();
    if (endNode.hasProperty(ID_PROPERTY_NAME)) {
      entity.setTargetId((String) endNode.getProperty(ID_PROPERTY_NAME));
    }
    entity.setTargetType(getPrimitiveType(endNode));
  }

  private String getPrimitiveType(Node node) {
    for (Label label : node.getLabels()) {
      String name = label.name();

      Class<? extends DomainEntity> type = typeRegistry.getDomainEntityType(name);
      if (TypeRegistry.isPrimitiveDomainEntity(type)) {
        return name;
      }
    }

    throw new CorruptNodeException(node.getProperty(ID_PROPERTY_NAME));
  }

}
