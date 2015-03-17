package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Maps;

class ExtendableRelationshipConverter<T extends Relation> implements RelationshipConverter<T>, ExtendablePropertyContainerConverter<Relationship, T> {

  private List<String> fieldsToIgnore;
  private Map<String, PropertyConverter> propertyConverters;

  public ExtendableRelationshipConverter(List<String> fieldsToIgnore) {
    this.fieldsToIgnore = fieldsToIgnore;
    propertyConverters = Maps.newHashMap();
  }

  @Override
  public void addValuesToPropertyContainer(Relationship relationship, T entity) throws ConversionException {
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      if (!shouldIgnoreField(propertyConverter)) {
        propertyConverter.setPropertyContainerProperty(relationship, entity);
      }
    }
  }

  @Override
  public void addValuesToEntity(T entity, Relationship relationship) throws ConversionException {
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      if (!shouldIgnoreField(propertyConverter)) {
        propertyConverter.addValueToEntity(entity, relationship);
      }
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
    String name = null;

    // TODO: find a neater way to determine the name of the primitive type see TIM-63
    for (Label label : node.getLabels()) {
      if (name == null || label.name().length() < name.length()) {
        name = label.name();
      }
    }
    return name;
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
      if (isRegularConverter(propertyConverter) && !shouldIgnoreField(propertyConverter)) {
        propertyConverter.setPropertyContainerProperty(relationship, entity);
      }
    }
  }

  private boolean isRegularConverter(PropertyConverter propertyConverter) {
    return FieldType.REGULAR == propertyConverter.getFieldType();
  }

  private boolean shouldIgnoreField(PropertyConverter propertyConverter) {
    return fieldsToIgnore.contains(propertyConverter.getName());
  }

  @Override
  public void updateModifiedAndRev(Relationship relationship, T entity) throws ConversionException {
    getPropertyConverter(REVISION_PROPERTY_NAME).setPropertyContainerProperty(relationship, entity);
    getPropertyConverter(MODIFIED_PROPERTY_NAME).setPropertyContainerProperty(relationship, entity);
  }

  private PropertyConverter getPropertyConverter(String fieldName) {
    return propertyConverters.get(fieldName);
  }

}
