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

  public <T extends Entity, U extends PropertyContainer> PropertyContainerConverter<T, U> createForTypeAndPropertyContainer(Class<T> type, Class<U> propertyContainerType) {
    if (Node.class.isAssignableFrom(propertyContainerType)) {
      @SuppressWarnings("unchecked")
      PropertyContainerConverter<T, U> propertyContainerConverter = (PropertyContainerConverter<T, U>) createEntityConverter(type, (Class<? extends Node>) propertyContainerType);
      addFieldWrappers(propertyContainerConverter, type);

      return propertyContainerConverter;
    } else if (Relation.class.isAssignableFrom(type) && Relationship.class.isAssignableFrom(propertyContainerType)) {
      @SuppressWarnings("unchecked")
      PropertyContainerConverter<T, U> entityConverter = (PropertyContainerConverter<T, U>) createRelationConverter((Class<? extends Relation>) type,
          (Class<? extends Relationship>) propertyContainerType);
      addFieldWrappers(entityConverter, type);
      return entityConverter;
    } else {
      return createNoOpPropertyContainerConverter(type, propertyContainerType);
    }
  }

  /**
   * Creates an EntityTypeWrapper for the primitive class of type.
   * @param type a DomainEntity type to find the primitive class for 
   * @return an EntityTypeWrapper for the primitive of type. This could be type itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends DomainEntity, U extends PropertyContainer> PropertyContainerConverter<? super T, U> createForPrimitive(Class<T> type, Class<U> propertyContainerType) {
    Class<? extends DomainEntity> primitive = TypeRegistry.toBaseDomainEntity(type);
    PropertyContainerConverter<? extends DomainEntity, U> propertyContainerConverter = this.createForTypeAndPropertyContainer(primitive, propertyContainerType);

    return (PropertyContainerConverter<? super T, U>) propertyContainerConverter;
  }

  @SuppressWarnings("unchecked")
  private <T extends Entity, U extends PropertyContainer> void addFieldWrappers(PropertyContainerConverter<T, U> propertyContainerConverter, Class<T> type) {
    for (Class<? extends Entity> typeToGetFieldsFrom = type; isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        propertyContainerConverter.addFieldConverter(fieldWrapperFactory.wrap(type, field));
      }
    }
  }

  private boolean isEntity(Class<? extends Entity> typeToGetFieldsFrom) {
    return Entity.class.isAssignableFrom(typeToGetFieldsFrom);
  }

  protected <T extends Entity, U extends Node> PropertyContainerConverter<T, U> createEntityConverter(Class<T> type, Class<U> nodeType) {
    return new RegularEntityConverter<T, U>(type);
  }

  protected <T extends Entity, U extends PropertyContainer> PropertyContainerConverter<T, U> createNoOpPropertyContainerConverter(Class<T> type, Class<U> propertyContainerType) {
    return new NoOpPropertyContainerConverter<T, U>();
  }

  protected <T extends Relation, U extends Relationship> PropertyContainerConverter<T, U> createRelationConverter(Class<T> type, Class<U> relationType) {
    ArrayList<String> fieldsToIgnore = Lists.newArrayList(Relation.SOURCE_ID, Relation.TARGET_ID, Relation.SOURCE_TYPE, Relation.TARGET_TYPE);
    return new RelationConverter<T, U>(fieldsToIgnore);
  }
}
