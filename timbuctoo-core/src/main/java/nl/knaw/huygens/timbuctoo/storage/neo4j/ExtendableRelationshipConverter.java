package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;

class ExtendableRelationshipConverter<T extends Relation> implements RelationshipConverter<T>, ExtendablePropertyContainerConverter<Relationship, T> {

  private List<String> fieldsToIgnore;
  private List<PropertyConverter> propertyConverters;

  public ExtendableRelationshipConverter(List<String> fieldsToIgnore) {
    this.fieldsToIgnore = fieldsToIgnore;
    propertyConverters = Lists.newArrayList();
  }

  @Override
  public void addValuesToPropertyContainer(Relationship relationship, T entity) throws ConversionException {
    for (PropertyConverter propertyConverter : propertyConverters) {
      if (!fieldsToIgnore.contains(propertyConverter.getName())) {
        propertyConverter.setPropertyContainerProperty(relationship, entity);
      }
    }
  }

  @Override
  public void addValuesToEntity(T entity, Relationship relationship) throws ConversionException {
    for (PropertyConverter propertyConverter : propertyConverters) {
      if (!fieldsToIgnore.contains(propertyConverter.getName())) {
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
    this.propertyConverters.add(fieldConverter);
  }

  @Override
  public void updatePropertyContainer(Relationship relationship, T entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void updateModifiedAndRev(Relationship relationship, T entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
