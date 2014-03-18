package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

public abstract class FieldMetaDataGenerator {

  protected static final String TYPE_FIELD = "type";
  protected static final String VALUE_FIELD = "value";
  protected final TypeNameGenerator typeNameGenerator;
  private final FieldMapper fieldMapper;
  private final Class<?> containingType;

  public FieldMetaDataGenerator(Class<?> containingType, TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper) {
    this.typeNameGenerator = typeNameGenerator;
    this.fieldMapper = fieldMapper;
    this.containingType = containingType;

  }

  /**
   * Add the metadata to the map of the {@code containingType}
   * @param mapToAddTo
   * @param field the field to get the metadata from.
   */
  public void addMetaDataToMap(Map<String, Object> mapToAddTo, Field field) {

    Map<String, Object> value = constructValue(field);

    mapToAddTo.put(getFieldName(containingType, field), value);
  }

  protected abstract Map<String, Object> constructValue(Field field);

  private String getFieldName(Class<?> type, Field field) {
    return fieldMapper.getFieldName(type, field);
  }
}
