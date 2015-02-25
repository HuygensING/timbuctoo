package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

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
  private <T extends Entity> void addFieldWrappers(EntityTypeWrapper<T> objectWrapper, Class<T> type) {
    for (Field field : type.getDeclaredFields()) {
      objectWrapper.addFieldWrapper(fieldWrapperFactory.wrap(type, field));
    }
    if (type != Entity.class) {
      addFieldWrappers(objectWrapper, (Class<T>) type.getSuperclass());
    }
  }

  protected <T extends Entity> EntityTypeWrapper<T> createEntityWrapper(Class<T> type) {
    return new EntityTypeWrapper<T>();
  }
}
