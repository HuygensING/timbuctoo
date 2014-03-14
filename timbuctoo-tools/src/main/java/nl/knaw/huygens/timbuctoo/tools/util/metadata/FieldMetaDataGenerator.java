package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

public abstract class FieldMetaDataGenerator {

  protected final TypeNameGenerator typeNameGenerator;
  private final FieldMapper fieldMapper;

  public FieldMetaDataGenerator(TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper) {
    this.typeNameGenerator = typeNameGenerator;
    this.fieldMapper = fieldMapper;

  }

  /**
   * Add the metadata to the map of the {@code containingType}
   * @param mapToAddTo
   * @param field the field to get the metadata from.
   * @param containingType the type that contains the field.
   */
  public void addMetaDataToMap(Map<String, Object> mapToAddTo, Field field, Class<?> containingType) {

    Object value = constructValue(field);

    mapToAddTo.put(getFieldName(containingType, field), value);
  }

  protected abstract Object constructValue(Field field);

  private String getFieldName(Class<?> type, Field field) {
    return fieldMapper.getFieldName(type, field);
  }
}
