package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import com.google.inject.Inject;

public class EntityWrapperFactory {

  private FieldWrapperFactory fieldWrapperFactory;
  private final IdGenerator idGenerator;

  @Inject
  public EntityWrapperFactory(FieldWrapperFactory fieldWrapperFactory, IdGenerator idGenerator) {
    this.fieldWrapperFactory = fieldWrapperFactory;
    this.idGenerator = idGenerator;
  }

  public <T extends Entity> EntityWrapper<T> createFromInstance(Class<T> type, T entity) {
    //    Class<? extends SystemEntity> type = entity.getClass();
    Change newChange = newChange();

    EntityWrapper<T> entityWrapper = createEntityWrapper(type);
    entityWrapper.setEntity(entity);
    entityWrapper.setCreated(newChange);
    entityWrapper.setModified(newChange);
    entityWrapper.setRev(newRevision());
    entityWrapper.setId(idGenerator.nextIdFor(type));

    addFieldWrappers(entityWrapper, type, entity);

    return entityWrapper;
  }

  public <T extends Entity> EntityWrapper<T> createFromType(Class<T> type) {
    // TODO Auto-generated method stub
    return null;
  }

  @SuppressWarnings("unchecked")
  private <T extends Entity> void addFieldWrappers(EntityWrapper<T> objectWrapper, Class<T> type, T instance) {
    for (Field field : type.getDeclaredFields()) {
      objectWrapper.addFieldWrapper(fieldWrapperFactory.wrap(type, instance, field));
    }
    if (type != Entity.class) {
      addFieldWrappers(objectWrapper, (Class<T>) type.getSuperclass(), instance);
    }
  }

  protected <T extends Entity> EntityWrapper<T> createEntityWrapper(Class<T> type) {
    return new EntityWrapper<T>();
  }

  protected Change newChange() {
    return Change.newInternalInstance();
  }

  protected int newRevision() {
    return 1;
  }

}
