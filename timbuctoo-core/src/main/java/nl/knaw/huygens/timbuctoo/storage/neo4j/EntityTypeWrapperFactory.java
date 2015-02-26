package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.inject.Inject;

public class EntityTypeWrapperFactory {

  private FieldWrapperFactory fieldWrapperFactory;

  @Inject
  public EntityTypeWrapperFactory(FieldWrapperFactory fieldWrapperFactory) {
    this.fieldWrapperFactory = fieldWrapperFactory;
  }

  public <T extends Entity> EntityTypeWrapper<T> createFromType(Class<T> type) {
    EntityTypeWrapper<T> entityWrapper = createEntityWrapper(type);
    addFieldWrappers(entityWrapper, type);

    return entityWrapper;
  }

  @SuppressWarnings("unchecked")
  public <T extends DomainEntity> EntityTypeWrapper<? super T> createForPrimitive(Class<T> type) {
    Class<? extends DomainEntity> primitive = TypeRegistry.toBaseDomainEntity(type);
    EntityTypeWrapper<? extends DomainEntity> entityTypeWrapper = this.createFromType(primitive);

    return (EntityTypeWrapper<? super T>) entityTypeWrapper;
  }

  @SuppressWarnings("unchecked")
  private <T extends Entity> void addFieldWrappers(EntityTypeWrapper<T> objectWrapper, Class<T> type) {
    for (Class<? extends Entity> typeToGetFieldsFrom = type; isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        objectWrapper.addFieldWrapper(fieldWrapperFactory.wrap(type, field));
      }
    }
  }

  private boolean isEntity(Class<? extends Entity> typeToGetFieldsFrom) {
    return Entity.class.isAssignableFrom(typeToGetFieldsFrom);
  }

  protected <T extends Entity> EntityTypeWrapper<T> createEntityWrapper(Class<T> type) {
    return new EntityTypeWrapper<T>();
  }
}
