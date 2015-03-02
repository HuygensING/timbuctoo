package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.inject.Inject;

public class EntityConverterFactory {

  private FieldConverterFactory fieldWrapperFactory;

  @Inject
  public EntityConverterFactory(FieldConverterFactory fieldWrapperFactory) {
    this.fieldWrapperFactory = fieldWrapperFactory;
  }

  public <T extends Entity> EntityConverter<T> createForType(Class<T> type) {
    EntityConverter<T> entityWrapper = createEntityConverter(type);
    addFieldWrappers(entityWrapper, type);

    return entityWrapper;
  }

  /**
   * Creates an EntityTypeWrapper for the primitive class of type.
   * @param type a DomainEntity type to find the primitive class for 
   * @return an EntityTypeWrapper for the primitive of type. This could be type itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends DomainEntity> EntityConverter<? super T> createForPrimitive(Class<T> type) {
    Class<? extends DomainEntity> primitive = TypeRegistry.toBaseDomainEntity(type);
    EntityConverter<? extends DomainEntity> entityTypeWrapper = this.createForType(primitive);

    return (EntityConverter<? super T>) entityTypeWrapper;
  }

  @SuppressWarnings("unchecked")
  private <T extends Entity> void addFieldWrappers(EntityConverter<T> objectWrapper, Class<T> type) {
    for (Class<? extends Entity> typeToGetFieldsFrom = type; isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        objectWrapper.addFieldWrapper(fieldWrapperFactory.wrap(type, field));
      }
    }
  }

  private boolean isEntity(Class<? extends Entity> typeToGetFieldsFrom) {
    return Entity.class.isAssignableFrom(typeToGetFieldsFrom);
  }

  protected <T extends Entity> EntityConverter<T> createEntityConverter(Class<T> type) {
    return new EntityConverter<T>(type);
  }
}
