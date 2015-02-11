package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import com.google.inject.Inject;

public class ObjectWrapperFactory {

  private FieldWrapperFactory fieldWrapperFactory;

  @Inject
  public ObjectWrapperFactory(FieldWrapperFactory fieldWrapperFactory) {
    this.fieldWrapperFactory = fieldWrapperFactory;
  }

  public ObjectWrapper wrap(SystemEntity entity) {

    ObjectWrapper objectWrapper = createObjectWrapper();

    addFieldWrappers(objectWrapper, entity.getClass(), entity);

    return objectWrapper;
  }

  @SuppressWarnings("unchecked")
  private void addFieldWrappers(ObjectWrapper objectWrapper, Class<? extends Entity> type, SystemEntity instance) {
    for (Field field : type.getDeclaredFields()) {
      objectWrapper.addFieldWrapper(fieldWrapperFactory.wrap(field, instance));
    }
    if (type != Entity.class) {
      addFieldWrappers(objectWrapper, (Class<? extends Entity>) type.getSuperclass(), instance);
    }
  }

  protected ObjectWrapper createObjectWrapper() {
    return new ObjectWrapper();
  }

}
