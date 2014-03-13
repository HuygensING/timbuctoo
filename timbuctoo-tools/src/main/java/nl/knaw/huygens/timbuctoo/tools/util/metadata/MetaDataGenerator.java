package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MetaDataGenerator {
  private final FieldMapper fieldMapper;
  private final TypeNameGenerator typeNameGenerator;

  public MetaDataGenerator(FieldMapper fieldMapper) {
    this.fieldMapper = fieldMapper;
    this.typeNameGenerator = new TypeNameGenerator();
  }

  public Map<String, Object> generate(Class<?> type) throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> metadataMap = Maps.newTreeMap();

    if (!isAbstract(type)) {
      for (Field field : getFields(type)) {
        String fieldName = getFieldName(type, field);
        if (field.getType().isEnum()) {
          metadataMap.put(fieldName, getEnumValues(field.getType()));
        } else if (isFinalField(field) && isStaticField(field)) {
          addConstantToMap(metadataMap, field, fieldName);
        } else if (!isStaticField(field)) {
          metadataMap.put(fieldName, typeNameGenerator.getTypeName(field));
        }
      }
    }

    return metadataMap;
  }

  private List<String> getEnumValues(Class<?> type) {
    List<String> enumValues = Lists.newArrayList();

    for (Object value : type.getEnumConstants()) {
      enumValues.add(value.toString());
    }

    return enumValues;
  }

  private void addConstantToMap(Map<String, Object> metadataMap, Field field, String fieldName) throws IllegalArgumentException, IllegalAccessException {
    // to get the values of private constants
    field.setAccessible(true);
    String value = String.format("%s <%s>", typeNameGenerator.getTypeName(field), field.get(null));
    metadataMap.put(fieldName, value);

  }

  private String getFieldName(Class<?> type, Field field) {
    return fieldMapper.getFieldName(type, field);
  }

  private boolean isStaticField(Field field) {
    return Modifier.isStatic(field.getModifiers());
  }

  private boolean isAbstract(Class<?> type) {
    return Modifier.isAbstract(type.getModifiers()) || type.isInterface();
  }

  private boolean isFinalField(Field field) {
    return Modifier.isFinal(field.getModifiers());
  }

  private List<Field> getFields(Class<?> type) {

    List<Field> fields = Lists.newArrayList(type.getDeclaredFields());

    if (!Object.class.equals(type.getSuperclass())) {
      fields.addAll(getFields(type.getSuperclass()));
    }

    return fields;
  }

}