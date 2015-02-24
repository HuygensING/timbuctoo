package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import com.google.inject.Inject;

public class EntityTypeWrapperFactory {

  private FieldWrapperFactory fieldWrapperFactory;
  private final IdGenerator idGenerator;

  @Inject
  public EntityTypeWrapperFactory(FieldWrapperFactory fieldWrapperFactory, IdGenerator idGenerator) {
    this.fieldWrapperFactory = fieldWrapperFactory;
    this.idGenerator = idGenerator;
  }

  public <T extends Entity> EntityTypeWrapper<T> createFromType(Class<T> type) {
    Change newChange = newChange();

    EntityTypeWrapper<T> entityWrapper = createEntityWrapper(type);
    entityWrapper.setCreated(newChange);
    entityWrapper.setModified(newChange);
    entityWrapper.setRev(newRevision());
    entityWrapper.setId(idGenerator.nextIdFor(type));

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

  protected Change newChange() {
    return Change.newInternalInstance();
  }

  protected int newRevision() {
    return 1;
  }

}
