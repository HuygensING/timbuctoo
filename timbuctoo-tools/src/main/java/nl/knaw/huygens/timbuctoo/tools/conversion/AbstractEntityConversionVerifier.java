package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import com.google.common.collect.Lists;

public abstract class AbstractEntityConversionVerifier<T extends Entity> implements EntityConversionVerifier {

  protected abstract T getOldItem(String oldId) throws StorageException;

  protected T getNewItem(Object internalId) throws StorageException {
    return graphStorage.getEntityByVertexId(type, internalId);
  }

  protected final MongoStorage mongoStorage;
  protected final TinkerPopConversionStorage graphStorage;
  private final PropertyVerifier propertyVerifier;
  private final List<Field> fields;
  protected final Class<T> type;

  public AbstractEntityConversionVerifier(Class<T> type, MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage, PropertyVerifier propertyVerifier) {
    this.type = type;
    this.mongoStorage = mongoStorage;
    this.graphStorage = graphStorage;
    this.propertyVerifier = propertyVerifier;
    this.fields = collectAllFields(type);
  }

  protected PropertyVerifier getPropertyVerifier() {
    return propertyVerifier;
  }

  protected List<Field> getFields() {
    return fields;
  }

  @SuppressWarnings("unchecked")
  private List<Field> collectAllFields(Class<? extends Entity> type) {
    List<Field> fields = Lists.newArrayList();
    for (Class<? extends Entity> typeToGetFieldsFrom = type; TypeRegistry.isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        if (!isIdField(field) && !Modifier.isStatic(field.getModifiers())) {
          fields.add(field);
        }
      }
    }
    return fields;
  }

  @Override
  public boolean isIdField(Field field) {
    return Objects.equals(field.getName(), "id");
  }

  @Override
  public final void verifyConversion(String oldId, String newId, Object newInternalId) throws StorageException, IllegalArgumentException, IllegalAccessException {
    T mongoEntity = getOldItem(oldId);
    T graphEntity = getNewItem(newInternalId);

    for (Field field : getFields()) {
      field.setAccessible(true);

      Object oldValue = field.get(mongoEntity);
      Object newValue = field.get(graphEntity);

      getPropertyVerifier().check(field.getName(), oldValue, newValue);
    }

    if (getPropertyVerifier().hasInconsistentProperties()) {
      throw new VerificationException(oldId, newId, getPropertyVerifier().getMismatches());
    }
  }

}