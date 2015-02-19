package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
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

  public EntityWrapper createFromInstance(SystemEntity entity) {
    Class<? extends SystemEntity> type = entity.getClass();
    Change newChange = newChange();

    EntityWrapper entityWrapper = createEntityWrapper();
    entityWrapper.setEntity(entity);
    entityWrapper.setCreated(newChange);
    entityWrapper.setModified(newChange);
    entityWrapper.setRev(newRevision());
    entityWrapper.setId(idGenerator.nextIdFor(type));

    addFieldWrappers(entityWrapper, type, entity);

    return entityWrapper;
  }

  public EntityWrapper createFromType(Class<? extends Entity> type) {
    // TODO Auto-generated method stub
    return null;
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

  protected Change newChange() {
    return Change.newInternalInstance();
  }

  protected int newRevision() {
    return 1;
  }

}
