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

  public Map<String, String> generate(Class<?> type) {
    Map<String, String> metadataMap = Maps.newTreeMap();

    if (!isAbstract(type)) {
      for (Field field : getFields(type)) {
        if (isStaticField(field)) {
          metadataMap.put(getFieldName(type, field), getTypeName(field));
        }
      }
    }

    return metadataMap;
  }

  protected String getFieldName(Class<?> type, Field field) {
    return fieldMapper.getFieldName(type, field);
  }

  protected boolean isStaticField(Field field) {
    return !Modifier.isStatic(field.getModifiers());
  }

  protected boolean isAbstract(Class<?> type) {
    return Modifier.isAbstract(type.getModifiers()) || type.isInterface();
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

  protected void addGenericData(ParameterizedType type, StringBuilder fieldName) {

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

  protected void appendParamType(StringBuilder fieldName, Type paramType) {

    if (paramType instanceof ParameterizedType) {
      ParameterizedType genericType = (ParameterizedType) paramType;
      appendParamType(fieldName, genericType.getRawType());
      addGenericData(genericType, fieldName);
    } else if (paramType instanceof Class<?>) {
      fieldName.append(((Class<?>) paramType).getSimpleName());
    }
  }

  protected boolean hasTypeParameters(Field field) {
    return field.getType().getTypeParameters() != null && field.getType().getTypeParameters().length > 0;
  }
}