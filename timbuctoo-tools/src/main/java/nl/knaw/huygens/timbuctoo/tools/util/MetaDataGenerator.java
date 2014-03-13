package nl.knaw.huygens.timbuctoo.tools.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MetaDataGenerator {
  private final FieldMapper fieldMapper;

  public MetaDataGenerator(FieldMapper fieldMapper) {
    this.fieldMapper = fieldMapper;
  }

  public Map<String, Object> generate(Class<?> type) throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> metadataMap = Maps.newTreeMap();

    if (!isAbstract(type) && !type.isEnum()) {
      for (Field field : getFields(type)) {
        String fieldName = getFieldName(type, field);
        if (field.getType().isEnum()) {
          metadataMap.put(fieldName, getEnumValues(field.getType()));
        } else if (isFinalField(field) && isStaticField(field)) {
          addConstantToMap(metadataMap, field, fieldName);
        } else if (!isStaticField(field)) {
          metadataMap.put(fieldName, getTypeName(field));
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
    String value = String.format("%s <%s>", getTypeName(field), field.get(null));
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

  private String getTypeName(Field field) {
    StringBuilder fieldName = new StringBuilder(field.getType().getSimpleName());

    if (hasTypeParameters(field)) {
      addGenericData((ParameterizedType) field.getGenericType(), fieldName);
    }

    return fieldName.toString();
  }

  private void addGenericData(ParameterizedType type, StringBuilder fieldName) {

    fieldName.append(" of (");

    boolean isFirst = true;
    for (Type paramType : type.getActualTypeArguments()) {
      if (!isFirst) {
        fieldName.append(", ");
      }
      isFirst = false;
      appendParamType(fieldName, paramType);
    }

    fieldName.append(")");

  }

  private void appendParamType(StringBuilder fieldName, Type paramType) {

    if (paramType instanceof ParameterizedType) {
      ParameterizedType genericType = (ParameterizedType) paramType;
      appendParamType(fieldName, genericType.getRawType());
      addGenericData(genericType, fieldName);
    } else if (paramType instanceof Class<?>) {
      fieldName.append(((Class<?>) paramType).getSimpleName());
    }
  }

  private boolean hasTypeParameters(Field field) {
    return field.getType().getTypeParameters() != null && field.getType().getTypeParameters().length > 0;
  }
}