package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.PropertyContainer;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class PropertyContainerConverterFactory {

  private FieldConverterFactory fieldWrapperFactory;

  @Inject
  public PropertyContainerConverterFactory(FieldConverterFactory fieldWrapperFactory) {
    this.fieldWrapperFactory = fieldWrapperFactory;
  }

  public <T extends Relation> RelationshipConverter<T> createForRelation(Class<T> type) {
    SimpleRelationshipConverter<T> relationshipConverter = createSimpleRelationshipConverter(type);
    addFieldWrappers(relationshipConverter, type);

    return relationshipConverter;
  }

  @SuppressWarnings("unchecked")
  public <T extends Relation> RelationshipConverter<? super T> createForPrimitiveRelation(Class<T> type) {
    Class<? extends Relation> primitive = (Class<? extends Relation>) TypeRegistry.toBaseDomainEntity(type);
    RelationshipConverter<? extends Relation> propertyContainerConverter = this.createForRelation(primitive);

    return (RelationshipConverter<? super T>) propertyContainerConverter;

  }

  public <T extends Entity> NodeConverter<T> createForType(Class<T> type) {
    SimpleNodeConverter<T> propertyContainerConverter = createSimpleNodeConverter(type);
    addFieldWrappers(propertyContainerConverter, type);

    return propertyContainerConverter;
  }

  /**
   * Creates an EntityTypeWrapper for the primitive class of type.
   * @param type a DomainEntity type to find the primitive class for
   * @return an EntityTypeWrapper for the primitive of type. This could be type itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends DomainEntity> NodeConverter<? super T> createForPrimitive(Class<T> type) {
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
  private <U extends PropertyContainer, T extends Entity> void addFieldWrappers(SimplePropertyContainerConverter<U, T> propertyContainerConverter, Class<T> type) {
    for (Class<? extends Entity> typeToGetFieldsFrom = type; isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        propertyContainerConverter.addFieldConverter(fieldWrapperFactory.wrap(type, field));
      }
    }
  }

  private boolean isEntity(Class<? extends Entity> typeToGetFieldsFrom) {
    return Entity.class.isAssignableFrom(typeToGetFieldsFrom);
  }

  protected <T extends Entity> SimpleNodeConverter<T> createSimpleNodeConverter(Class<T> type) {
    return new SimpleNodeConverter<T>(type);
  }

  protected <T extends Relation> SimpleRelationshipConverter<T> createSimpleRelationshipConverter(Class<T> type) {
    ArrayList<String> fieldsToIgnore = Lists.newArrayList(Relation.SOURCE_ID, Relation.TARGET_ID, Relation.SOURCE_TYPE, Relation.TARGET_TYPE);
    return new SimpleRelationshipConverter<T>(fieldsToIgnore);
  }

}
