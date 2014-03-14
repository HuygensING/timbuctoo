package nl.knaw.huygens.timbuctoo.tools.util.metadata;

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
  private final TypeNameGenerator typeNameGenerator;
  private List<Class<?>> innerClasses;

  public MetaDataGenerator(FieldMapper fieldMapper) {
    this.fieldMapper = fieldMapper;
    this.typeNameGenerator = new TypeNameGenerator();
  }

  public Map<String, Object> generate(Class<?> type) throws IllegalArgumentException, IllegalAccessException {
    Map<String, Object> metadataMap = Maps.newTreeMap();

    if (!isAbstract(type)) {
      innerClasses = getInnerClasses(type);
      for (Field field : getFields(type)) {
        FieldMetaDataGenerator fieldMetaDataGenerator = getFieldMetaDataGenerator(field);

        fieldMetaDataGenerator.addMetaDataToMap(metadataMap, field, type);

      }
    }

    return metadataMap;
  }

  private List<Class<?>> getInnerClasses(Class<?> type) {
    List<Class<?>> classes = Lists.newArrayList(type.getDeclaredClasses());

    if (!Object.class.equals(type.getSuperclass())) {
      classes.addAll(getInnerClasses(type.getSuperclass()));
    }

    return classes;
  }

  private List<Field> getFields(Class<?> type) {

    List<Field> fields = Lists.newArrayList(type.getDeclaredFields());

    if (!Object.class.equals(type.getSuperclass())) {
      fields.addAll(getFields(type.getSuperclass()));
    }

    return fields;
  }

  private boolean isConstantField(Field field) {
    return isFinalField(field) && isStaticField(field);
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

  private boolean isEnumValueField(Class<?> type, Field field) {
    if (!hasTypeParameters(type)) {
      return type.isEnum();
    }

    boolean isEnum = false;
    for (Type paramType : ((ParameterizedType) field.getGenericType()).getActualTypeArguments()) {

      if (paramType instanceof Class<?>) {
        isEnum |= isEnumValueField((Class<?>) paramType, field);
      }
    }
    return isEnum;

  }

  private boolean hasTypeParameters(Class<?> type) {
    return type.getTypeParameters() != null && type.getTypeParameters().length > 0;
  }

  private FieldMetaDataGenerator getFieldMetaDataGenerator(Field field) {
    if (isEnumValueField(field.getType(), field)) {
      return new EnumValueFieldMetaDataGenerator(typeNameGenerator, fieldMapper);
    } else if (isConstantField(field)) {
      return new ConstantFieldMetadataGenerator(typeNameGenerator, fieldMapper);
    } else if (isPoorMansEnumField(field)) {
      return new PoorMansEnumFieldMetadataGenerator(typeNameGenerator, fieldMapper, getPoorMansEnumType(field));
    } else if (!isStaticField(field)) {
      return new DefaultFieldMetadataGenerator(typeNameGenerator, fieldMapper);
    } else {
      return new NoOpFieldMetaDataGenerator(typeNameGenerator, fieldMapper);
    }
  }

  private Class<?> getPoorMansEnumType(Field field) {
    String fieldName = field.getName();
    for (Class<?> innerClass : innerClasses) {
      if (isMatchingName(fieldName, innerClass)) {
        return innerClass;
      }
    }
    return null;
  }

  private boolean isPoorMansEnumField(Field field) {
    String fieldName = field.getName();
    for (Class<?> innerClass : innerClasses) {
      if (isMatchingName(fieldName, innerClass)) {
        return true;
      }
    }
    return false;
  }

  private boolean isMatchingName(String fieldName, Class<?> innerClass) {
    return fieldName.equalsIgnoreCase(innerClass.getSimpleName()) || fieldName.equalsIgnoreCase(innerClass.getSimpleName() + "s");
  }
}