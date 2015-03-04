package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;

import com.google.inject.Inject;

public class EntityConverterFactory {

  private FieldConverterFactory fieldWrapperFactory;

  @Inject
  public EntityConverterFactory(FieldConverterFactory fieldWrapperFactory) {
    this.fieldWrapperFactory = fieldWrapperFactory;
  }

  public <T extends Entity, U extends PropertyContainer> EntityConverter<T, U> createForTypeAndPropertyContainer(Class<T> type, Class<U> propertyContainerType) {
    if (Node.class.isAssignableFrom(propertyContainerType)) {
      @SuppressWarnings("unchecked")
      EntityConverter<T, U> entityWrapper = (EntityConverter<T, U>) createEntityConverter(type, (Class<? extends Node>) propertyContainerType);
      addFieldWrappers(entityWrapper, type);

      return entityWrapper;
    } else {
      return createNoOpEntityConverter(type, propertyContainerType);
    }
  }

  /**
   * Creates an EntityTypeWrapper for the primitive class of type.
   * @param type a DomainEntity type to find the primitive class for 
   * @return an EntityTypeWrapper for the primitive of type. This could be type itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends DomainEntity, U extends PropertyContainer> EntityConverter<? super T, U> createForPrimitive(Class<T> type, Class<U> propertyContainerType) {
    Class<? extends DomainEntity> primitive = TypeRegistry.toBaseDomainEntity(type);
    EntityConverter<? extends DomainEntity, U> entityTypeWrapper = this.createForTypeAndPropertyContainer(primitive, propertyContainerType);

    return (EntityConverter<? super T, U>) entityTypeWrapper;
  }

  @SuppressWarnings("unchecked")
  private <T extends Entity, U extends PropertyContainer> void addFieldWrappers(EntityConverter<T, U> objectWrapper, Class<T> type) {
    for (Class<? extends Entity> typeToGetFieldsFrom = type; isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        objectWrapper.addFieldConverter(fieldWrapperFactory.wrap(type, field));
      }
    }
  }

  private boolean isEntity(Class<? extends Entity> typeToGetFieldsFrom) {
    return Entity.class.isAssignableFrom(typeToGetFieldsFrom);
  }

  protected <T extends Entity, U extends Node> EntityConverter<T, U> createEntityConverter(Class<T> type, Class<U> nodeType) {
    return new RegularEntityConverter<T, U>(type);
  }

  protected <T extends Entity, U extends PropertyContainer> EntityConverter<T, U> createNoOpEntityConverter(Class<T> type, Class<U> propertyContainerType) {
    return new NoOpEntityConverter<T, U>();
  }
}
