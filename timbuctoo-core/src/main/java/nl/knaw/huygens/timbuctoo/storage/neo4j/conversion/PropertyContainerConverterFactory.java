package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion;

import java.lang.reflect.Field;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.neo4j.NodeConverter;
import nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipConverter;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.property.PropertyConverterFactory;

import org.neo4j.graphdb.PropertyContainer;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class PropertyContainerConverterFactory {

  private final PropertyConverterFactory propertyConverterFactory;
  private final TypeRegistry typeRegistry;

  @Inject
  public PropertyContainerConverterFactory(PropertyConverterFactory propertyConverterFactory, TypeRegistry typeRegistry) {
    this.propertyConverterFactory = propertyConverterFactory;
    this.typeRegistry = typeRegistry;
  }

  public PropertyContainerConverterFactory(TypeRegistry typeRegistry) {
    this.propertyConverterFactory = new PropertyConverterFactory();
    this.typeRegistry = typeRegistry;
  }

  public <T extends Relation> RelationshipConverter<T> createForRelation(Class<T> type) {
    ExtendableRelationshipConverter<T> relationshipConverter = createSimpleRelationshipConverter(type);
    addPropertyConverters(relationshipConverter, type);

    return relationshipConverter;
  }

  @SuppressWarnings("unchecked")
  private <T extends Relation> RelationshipConverter<? super T> createForPrimitiveRelation(Class<T> type) {
    Class<? extends Relation> primitive = (Class<? extends Relation>) TypeRegistry.toBaseDomainEntity(type);
    RelationshipConverter<? extends Relation> propertyContainerConverter = this.createForRelation(primitive);

    return (RelationshipConverter<? super T>) propertyContainerConverter;
  }

  public <T extends Relation> RelationshipConverter<T> createCompositeForRelation(Class<T> type) {
    List<RelationshipConverter<? super T>> relationshipConverters = Lists.newArrayList();
    relationshipConverters.add(createForRelation(type));
    relationshipConverters.add(createForPrimitiveRelation(type));

    return new CompositeRelationshipConverter<T>(relationshipConverters);
  }

  public <T extends Entity> NodeConverter<T> createForType(Class<T> type) {
    ExtendableNodeConverter<T> propertyContainerConverter = createSimpleNodeConverter(type);
    addPropertyConverters(propertyContainerConverter, type);

    return propertyContainerConverter;
  }

  /**
   * Creates a NodeConverter for the primitive class of type.
   * @param type a DomainEntity type to find the primitive class for
   * @return a NodeConverter for the primitive of type. This could be type itself.
   */
  @SuppressWarnings("unchecked")
  private <T extends DomainEntity> NodeConverter<? super T> createForPrimitive(Class<T> type) {
    Class<? extends DomainEntity> primitive = TypeRegistry.toBaseDomainEntity(type);
    NodeConverter<? extends DomainEntity> nodeConverter = this.createForType(primitive);

    return (NodeConverter<? super T>) nodeConverter;
  }

  /**
   * Creates a NodeConverter that handles the values of type,
   *  as well as the values of the primitive domain entity of type.
   * @param type the type to create the NodeConverter for 
   * @return a NodeConverter
   */
  public <T extends DomainEntity> NodeConverter<T> createCompositeForType(Class<T> type) {
    List<NodeConverter<? super T>> nodeConverters = Lists.newArrayList();
    nodeConverters.add(createForType(type));
    nodeConverters.add(createForPrimitive(type));

    CompositeNodeConverter<T> compositeNodeConverter = new CompositeNodeConverter<T>(nodeConverters);

    return compositeNodeConverter;
  }

  @SuppressWarnings("unchecked")
  private <U extends PropertyContainer, T extends Entity> void addPropertyConverters(ExtendablePropertyContainerConverter<U, T> propertyContainerConverter, Class<T> type) {
    for (Class<? extends Entity> typeToGetFieldsFrom = type; isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        propertyContainerConverter.addPropertyConverter(propertyConverterFactory.createFor(type, field));
      }
    }
  }

  private boolean isEntity(Class<? extends Entity> typeToGetFieldsFrom) {
    return Entity.class.isAssignableFrom(typeToGetFieldsFrom);
  }

  protected <T extends Entity> ExtendableNodeConverter<T> createSimpleNodeConverter(Class<T> type) {
    return new ExtendableNodeConverter<T>(type);
  }

  protected <T extends Relation> ExtendableRelationshipConverter<T> createSimpleRelationshipConverter(Class<T> type) {
    return new ExtendableRelationshipConverter<T>(typeRegistry);
  }

}
