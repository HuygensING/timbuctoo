package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import com.google.common.collect.Lists;

public class FieldMetaDataGeneratorFactory {
  private final TypeNameGenerator typeNameGenerator;
  private final FieldMapper fieldMapper;
  private Class<?> type;

  public FieldMetaDataGeneratorFactory(TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper) {
    this.typeNameGenerator = typeNameGenerator;
    this.fieldMapper = fieldMapper;
  }

  public void setType(Class<?> type) {
    this.type = type;
  }

  public FieldMetaDataGenerator createFieldMetaDataGenerator(Field field) {
    if (isEnumValueField(field.getType(), field)) {
      return new EnumValueFieldMetaDataGenerator(typeNameGenerator, fieldMapper);
    } else if (isConstantField(field)) {
      return new ConstantFieldMetaDataGenerator(typeNameGenerator, fieldMapper);
    } else if (isPoorMansEnumField(field)) {
      return new PoorMansEnumFieldMetaDataGenerator(typeNameGenerator, fieldMapper, getPoorMansEnumType(field));
    } else if (!isStaticField(field)) {
      return new DefaultFieldMetaDataGenerator(typeNameGenerator, fieldMapper);
    } else {
      return new NoOpFieldMetaDataGenerator(typeNameGenerator, fieldMapper);
    }
  }

  private Class<?> getPoorMansEnumType(Field field) {
    String fieldName = field.getName();
    for (Class<?> innerClass : getInnerClasses(type)) {
      if (isMatchingName(fieldName, innerClass)) {
        return innerClass;
      }
    }
    return null;
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

  private boolean isMatchingName(String fieldName, Class<?> innerClass) {
    return fieldName.equalsIgnoreCase(innerClass.getSimpleName()) || fieldName.equalsIgnoreCase(innerClass.getSimpleName() + "s");
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

}
