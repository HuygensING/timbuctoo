package nl.knaw.huygens.timbuctoo.tools.util.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeNameGenerator {
  public String getTypeName(Field field) {
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
