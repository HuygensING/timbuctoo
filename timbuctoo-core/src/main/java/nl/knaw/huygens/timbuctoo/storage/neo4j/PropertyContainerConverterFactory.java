package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;
import java.util.ArrayList;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class PropertyContainerConverterFactory {

  private FieldConverterFactory fieldWrapperFactory;

  @Inject
  public PropertyContainerConverterFactory(FieldConverterFactory fieldWrapperFactory) {
    this.fieldWrapperFactory = fieldWrapperFactory;
  }

  public <T extends Relation> RelationshipConverter<Relationship, T> createForRelation(Class<T> type) {
    RelationshipConverter<Relationship, T> relationshipConverter = createRelationshipConverter(Relationship.class, type);
    addFieldWrappers(relationshipConverter, type);

    return relationshipConverter;
  }

  @SuppressWarnings("unchecked")
  public <T extends Relation> RelationshipConverter<Relationship, ? super T> createForPrimitiveRelation(Class<T> type) {
    Class<? extends Relation> primitive = (Class<? extends Relation>) TypeRegistry.toBaseDomainEntity(type);
    RelationshipConverter<Relationship, ? extends Relation> propertyContainerConverter = this.createForRelation(primitive);

    return (RelationshipConverter<Relationship, ? super T>) propertyContainerConverter;

  }

  public <U extends PropertyContainer, T extends Entity> PropertyContainerConverter<U, T> createForTypeAndPropertyContainer(Class<U> propertyContainerType, Class<T> type) {
    @SuppressWarnings("unchecked")
    PropertyContainerConverter<U, T> propertyContainerConverter = (PropertyContainerConverter<U, T>) createNodeConverter((Class<? extends Node>) propertyContainerType, type);
    addFieldWrappers(propertyContainerConverter, type);

    return propertyContainerConverter;

  }

  /**
   * Creates an EntityTypeWrapper for the primitive class of type.
   * @param type a DomainEntity type to find the primitive class for
   * @return an EntityTypeWrapper for the primitive of type. This could be type itself.
   */
  @SuppressWarnings("unchecked")
  public <U extends PropertyContainer, T extends DomainEntity> PropertyContainerConverter<U, ? super T> createForPrimitive(Class<U> propertyContainerType, Class<T> type) {
    Class<? extends DomainEntity> primitive = TypeRegistry.toBaseDomainEntity(type);
    PropertyContainerConverter<U, ? extends DomainEntity> propertyContainerConverter = this.createForTypeAndPropertyContainer(propertyContainerType, primitive);

    return (PropertyContainerConverter<U, ? super T>) propertyContainerConverter;
  }

  @SuppressWarnings("unchecked")
  private <U extends PropertyContainer, T extends Entity> void addFieldWrappers(PropertyContainerConverter<U, T> propertyContainerConverter, Class<T> type) {
    for (Class<? extends Entity> typeToGetFieldsFrom = type; isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        propertyContainerConverter.addFieldConverter(fieldWrapperFactory.wrap(type, field));
      }
    }
  }

  private boolean isEntity(Class<? extends Entity> typeToGetFieldsFrom) {
    return Entity.class.isAssignableFrom(typeToGetFieldsFrom);
  }

  protected <U extends Node, T extends Entity> PropertyContainerConverter<U, T> createNodeConverter(Class<U> nodeType, Class<T> type) {
    return new NodeConverter<U, T>(type);
  }

  protected <U extends PropertyContainer, T extends Entity> PropertyContainerConverter<U, T> createNoOpPropertyContainerConverter(Class<U> propertyContainerType, Class<T> type) {
    return new NoOpPropertyContainerConverter<U, T>();
  }

  protected <U extends Relationship, T extends Relation> RelationshipConverter<U, T> createRelationshipConverter(Class<U> relationType, Class<T> type) {
    ArrayList<String> fieldsToIgnore = Lists.newArrayList(Relation.SOURCE_ID, Relation.TARGET_ID, Relation.SOURCE_TYPE, Relation.TARGET_TYPE);
    return new RelationshipConverter<U, T>(fieldsToIgnore);
  }
}
