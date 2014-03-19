package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import com.google.common.collect.Lists;

public class TypeFacade {
  private final Class<?> type;
  private final FieldMapper fieldMapper;

  public TypeFacade(Class<?> type) {
    this(type, new FieldMapper());
  }

  public TypeFacade(Class<?> type, FieldMapper fieldMapper) {
    this.type = type;
    this.fieldMapper = fieldMapper;
  }

  public String getFieldName(Field field) {

    return this.fieldMapper.getFieldName(type, field);
  }

  public FieldType getFieldType(Field field) {
    if (isEnumValueField(field, field.getType())) {
      return FieldType.ENUM;
    } else if (isConstantField(field)) {
      return FieldType.CONSTANT;
    } else if (isPoorMansEnumField(field)) {
      return FieldType.POOR_MANS_ENUM;
    } else if (!isStaticField(field)) {
      return FieldType.DEFAULT;
    }

    return FieldType.UNKNOWN;

  }

  private boolean isEnumValueField(Field field, Class<?> type) {
    if (!hasTypeParameters(type)) {
      return type.isEnum();
    }

    boolean isEnum = false;
    for (Type paramType : ((ParameterizedType) field.getGenericType()).getActualTypeArguments()) {

      if (paramType instanceof Class<?>) {
        isEnum |= isEnumValueField(field, (Class<?>) paramType);
      }
    }
    return isEnum;

  }

  private boolean isConstantField(Field field) {
    return isFinalField(field) && isStaticField(field);
  }

  private boolean isStaticField(Field field) {
    return Modifier.isStatic(field.getModifiers());
  }

  private boolean isFinalField(Field field) {
    return Modifier.isFinal(field.getModifiers());
  }

  private boolean hasTypeParameters(Class<?> type) {
    return type.getTypeParameters() != null && type.getTypeParameters().length > 0;
  }

  public Class<?> getPoorMansEnumType(Field field) {
    String fieldName = field.getName();
    for (Class<?> innerClass : getInnerClasses(type)) {
      if (isMatchingName(fieldName, innerClass)) {
        return innerClass;
      }
    }
    return null;
  }

  private boolean isMatchingName(String fieldName, Class<?> innerClass) {
    return fieldName.equalsIgnoreCase(innerClass.getSimpleName()) || fieldName.equalsIgnoreCase(innerClass.getSimpleName() + "s");
  }

  private boolean isPoorMansEnumField(Field field) {
    return getPoorMansEnumType(field) != null;
  }

  private List<Class<?>> getInnerClasses(Class<?> type) {
    List<Class<?>> classes = Lists.newArrayList(type.getDeclaredClasses());

    if (!Object.class.equals(type.getSuperclass())) {
      classes.addAll(getInnerClasses(type.getSuperclass()));
    }

    return classes;
  }

  public enum FieldType {
    ENUM, CONSTANT, POOR_MANS_ENUM, DEFAULT, UNKNOWN
  }
}
