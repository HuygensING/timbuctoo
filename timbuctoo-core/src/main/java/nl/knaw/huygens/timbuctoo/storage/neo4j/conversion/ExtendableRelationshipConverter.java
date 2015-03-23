package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;

import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.neo4j.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipConverter;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Maps;

class ExtendableRelationshipConverter<T extends Relation> implements RelationshipConverter<T>, ExtendablePropertyContainerConverter<Relationship, T> {

  private final Map<String, PropertyConverter> propertyConverters;
  private final TypeRegistry typeRegistry;
  private final EntityInstantiator entityInstantiatorMock;
  private final Class<T> type;

  public ExtendableRelationshipConverter(Class<T> type, TypeRegistry typeRegistry, EntityInstantiator entityInstantiatorMock) {
    this.type = type;
    this.typeRegistry = typeRegistry;
    this.entityInstantiatorMock = entityInstantiatorMock;
    propertyConverters = Maps.newHashMap();
  }

  public ExtendableRelationshipConverter(Class<T> type, TypeRegistry typeRegistry) {
    this(type, typeRegistry, new EntityInstantiator());
  }

  @Override
  public void addValuesToPropertyContainer(Relationship relationship, T entity) throws ConversionException {
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      propertyConverter.setPropertyContainerProperty(relationship, entity);
    }
  }

  @Override
  public void addValuesToEntity(T entity, Relationship relationship) throws ConversionException {
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      propertyConverter.addValueToEntity(entity, relationship);
    }

    Node startNode = relationship.getStartNode();
    if (startNode.hasProperty(ID_PROPERTY_NAME)) {
      entity.setSourceId((String) startNode.getProperty(ID_PROPERTY_NAME));
    }

    entity.setSourceType(getPrimitiveType(startNode));

    Node endNode = relationship.getEndNode();
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

  @Override
  public void addPropertyConverter(PropertyConverter fieldConverter) {
    this.propertyConverters.put(fieldConverter.getName(), fieldConverter);
  }

  private Collection<PropertyConverter> getPropertyConverters() {
    return propertyConverters.values();
  }

  @Override
  public void updatePropertyContainer(Relationship relationship, T entity) throws ConversionException {
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      if (isRegularConverter(propertyConverter)) {
        propertyConverter.setPropertyContainerProperty(relationship, entity);
      }
    }
  }

  private boolean isRegularConverter(PropertyConverter propertyConverter) {
    return FieldType.REGULAR == propertyConverter.getFieldType();
  }

  @Override
  public void updateModifiedAndRev(Relationship relationship, T entity) throws ConversionException {
    getPropertyConverter(REVISION_PROPERTY_NAME).setPropertyContainerProperty(relationship, entity);
    getPropertyConverter(MODIFIED_PROPERTY_NAME).setPropertyContainerProperty(relationship, entity);
  }

  private PropertyConverter getPropertyConverter(String fieldName) {
    return propertyConverters.get(fieldName);
  }

  @Override
  public T convertToEntity(Relationship propertyContainer) throws ConversionException, InstantiationException {
    T entity = entityInstantiatorMock.createInstanceOf(type);

    addValuesToEntity(entity, propertyContainer);

    return entity;
  }

}
