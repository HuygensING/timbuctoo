package nl.knaw.huygens.timbuctoo.storage.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.annotations.DBProperty;
import nl.knaw.huygens.timbuctoo.model.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.REGULAR;
import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.VIRTUAL;
import static nl.knaw.huygens.timbuctoo.storage.graph.MethodHelper.getMethodByName;
import static nl.knaw.huygens.timbuctoo.storage.graph.MethodHelper.getGetterName;

public class PropertyBusinessRules {

  private boolean isVirtualProperty(Class<? extends Entity> containingType, Field field) {
    return isStatic(field);
  }

  private boolean isStatic(Field field) {
    return Modifier.isStatic(field.getModifiers());
  }

  public FieldType getFieldType(Class<? extends Entity> containingType, Field field) {
    if (isStatic(field)) {
      return VIRTUAL;
    }

    DBProperty dbProperty = field.getAnnotation(DBProperty.class);
    if (dbProperty != null) {
      return dbProperty.type();
    }

    return REGULAR;
  }

  /**
   * The name of the field used by the client.
   * @param containingType the type that contains the field
   * @param field the field to get the name for
   * @return the field name
   */
  public String getFieldName(Class<? extends Entity> containingType, Field field) {
    JsonProperty annotation = field.getAnnotation(JsonProperty.class);
    if (annotation != null) {
      return annotation.value();
    }

    Method method = getMethodByName(containingType, getGetterName(field));
    if (method != null && method.getAnnotation(JsonProperty.class) != null) {
      return method.getAnnotation(JsonProperty.class).value();
    }

    return field.getName();
  }

  /**
   * Gets the property name without any potential prefixes.
   * @param type the type to get the property name from
   * @param field the field to get the property name from
   * @return the property name
   */
  public String getPropertyName(Class<? extends Entity> type, Field field) {
    DBProperty annotation = field.getAnnotation(DBProperty.class);
    if(annotation != null){
      return annotation.value();
    }
    return field.getName();
  }
}
