package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import com.google.common.collect.Lists;

/**
 * A class that checks if the RelationType is correctly converted.
 */
public class EntityConversionChecker<T extends Entity> {

  private final MongoStorage mongoStorage;
  private final GraphStorage graphStorage;
  private PropertyVerifier propertyVerifier;
  private List<Field> fields;
  private Class<T> type;

  public EntityConversionChecker(Class<T> type, MongoStorage mongoStorage, GraphStorage graphStorage) {
    this(type, mongoStorage, graphStorage, new PropertyVerifier());
  }

  public EntityConversionChecker(Class<T> type, MongoStorage mongoStorage, GraphStorage graphStorage, PropertyVerifier propertyVerifier) {
    this.type = type;
    this.mongoStorage = mongoStorage;
    this.graphStorage = graphStorage;
    this.propertyVerifier = propertyVerifier;

    fields = collectAllFields(type);
  }

  public void verifyConversion(String oldId, String newId) throws StorageException, IllegalArgumentException, IllegalAccessException {
    T mongoEntity = mongoStorage.getEntity(type, oldId);
    T graphEntity = graphStorage.getEntity(type, newId);

    for (Field field : fields) {
      field.setAccessible(true);

      Object oldValue = field.get(mongoEntity);
      Object newValue = field.get(graphEntity);

      propertyVerifier.check(field.getName(), oldValue, newValue);
    }

    if (propertyVerifier.hasInconsistentProperties()) {
      throw new VerificationException(oldId, newId, propertyVerifier.getMismatches());
    }
  }

  @SuppressWarnings("unchecked")
  private List<Field> collectAllFields(Class<? extends Entity> type) {
    List<Field> fields = Lists.newArrayList();
    for (Class<? extends Entity> typeToGetFieldsFrom = type; TypeRegistry.isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        if (!isIdField(field)) {
          fields.add(field);
        }
      }
    }
    return fields;
  }

  public boolean isIdField(Field field) {
    return Objects.equals(field.getName(), "id");
  }

}
