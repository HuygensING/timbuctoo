package nl.knaw.huygens.timbuctoo.storage.mongo;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNameGenerator;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

/**
 * A class that contains all the information about how the class fields are mapped 
 * to the fields in the database.
 */
public class MongoFieldMapper {
  private static final Class<JsonProperty> ANNOTATION_TO_RETRIEVE = JsonProperty.class;
  private static final String GET_ACCESSOR = "get";
  private static final String IS_ACCESSOR = "is"; //get accesor for booleans.

  /**
   * Generates a field map with for the {@code type} with object fields as keys and database fields as values.
   * @param type the type to create the map for.
   * @return the field map.
   */
  public Map<String, String> getFieldMap(Class<?> type) {
    Map<String, String> map = Maps.newHashMap();
    for (Field field : type.getDeclaredFields()) {
      map.put(field.getName(), getFieldName(type, field));
    }

    return map;
  }

  public String getTypeNameOfFieldName(String fieldName) {
    checkNotNull(fieldName);

    return fieldName.contains(".") ? fieldName.substring(0, fieldName.indexOf('.')) : null;
  }

  /**
   * Gets a field name for a field in combination with a class. 
   * This method will add a prefix for every {@code type} that is not {@code Entity}, {@code DomainEntity} or {@code SystemEntity}.
   * The method will not check if the {@code type} contains the field. It will just create a {@code String). 
   * @param type the type needed for the prefix.
   * @param field the field to get the name for.
   * @return the field name.
   */
  public String getFieldName(Class<?> type, Field field) {
    JsonProperty annotation = field.getAnnotation(ANNOTATION_TO_RETRIEVE);

    if (annotation != null) {
      return getPrefixedFieldName(type, annotation.value());
    }

    Method method = getMethodOfField(type, field);

    if (method != null && method.getAnnotation(ANNOTATION_TO_RETRIEVE) != null) {
      return getPrefixedFieldName(type, method.getAnnotation(ANNOTATION_TO_RETRIEVE).value());
    }
    return getPrefixedFieldName(type, field.getName());
  }

  private String getPrefixedFieldName(Class<?> type, String fieldName) {
    if (type == Entity.class || type == DomainEntity.class || type == SystemEntity.class) {
      return fieldName;
    }

    return TypeNameGenerator.getInternalName(type) + "." + fieldName;
  }

  private Method getMethodOfField(Class<?> type, Field field) {
    Method method = null;
    String methodName = getMethodName(field);

    for (Method m : type.getMethods()) {
      if (m.getName().equals(methodName)) {
        method = m;
        break;
      }
    }

    return method;
  }

  private String getMethodName(Field field) {
    char[] fieldNameChars = field.getName().toCharArray();

    fieldNameChars[0] = Character.toUpperCase(fieldNameChars[0]);

    if (field.getType() == boolean.class || field.getType() == Boolean.class) {
      return IS_ACCESSOR.concat(String.valueOf(fieldNameChars));
    }

    return GET_ACCESSOR.concat(String.valueOf(fieldNameChars));
  }
}
