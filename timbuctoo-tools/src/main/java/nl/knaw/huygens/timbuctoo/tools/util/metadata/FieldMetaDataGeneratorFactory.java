package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

public class FieldMetaDataGeneratorFactory {
  private final TypeNameGenerator typeNameGenerator;
  private final FieldMapper fieldMapper;
  private final List<Class<?>> innerClasses;

  public FieldMetaDataGeneratorFactory(TypeNameGenerator typeNameGenerator, FieldMapper fieldMapper, List<Class<?>> innerClasses) {
    this.typeNameGenerator = typeNameGenerator;
    this.fieldMapper = fieldMapper;
    this.innerClasses = innerClasses;
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
