package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import com.google.inject.Inject;

public class EntityWrapperFactory {

  private FieldWrapperFactory fieldWrapperFactory;

  @Inject
  public EntityWrapperFactory(FieldWrapperFactory fieldWrapperFactory) {
    this.fieldWrapperFactory = fieldWrapperFactory;
  }

  public EntityWrapper wrap(SystemEntity entity) {

    EntityWrapper entityWrapper = createEntityWrapper();
    entityWrapper.setEntity(entity);

    addFieldWrappers(entityWrapper, entity.getClass(), entity);

    return entityWrapper;
  }

  @SuppressWarnings("unchecked")
  private void addFieldWrappers(EntityWrapper objectWrapper, Class<? extends Entity> type, SystemEntity instance) {
    for (Field field : type.getDeclaredFields()) {
      objectWrapper.addFieldWrapper(fieldWrapperFactory.wrap(field, instance));
    }
    if (type != Entity.class) {
      addFieldWrappers(objectWrapper, (Class<? extends Entity>) type.getSuperclass(), instance);
    }
  }

  protected EntityWrapper createEntityWrapper() {
    return new EntityWrapper();
  }

}
