package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import com.google.common.collect.Lists;

/**
 * A class that checks if the RelationType is correctly converted.
 */
public class RelationTypeConversionChecker {

  private final MongoStorage mongoStorage;
  private final GraphStorage graphStorage;
  private PropertyVerifier propertyVerifier;
  private List<Field> fields;

  public RelationTypeConversionChecker(MongoStorage mongoStorage, GraphStorage graphStorage) {
    this(mongoStorage, graphStorage, new PropertyVerifier());
  }

  public RelationTypeConversionChecker(MongoStorage mongoStorage, GraphStorage graphStorage, PropertyVerifier propertyVerifier) {
    this.mongoStorage = mongoStorage;
    this.graphStorage = graphStorage;
    this.propertyVerifier = propertyVerifier;

    fields = collectAllFields(RelationType.class);
  }

  public void verifyConversion(String oldId, String newId) throws StorageException, IllegalArgumentException, IllegalAccessException {
    Class<RelationType> type = RelationType.class;
    RelationType mongoEntity = mongoStorage.getEntity(type, oldId);
    RelationType graphEntity = graphStorage.getEntity(type, newId);

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
