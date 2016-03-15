package nl.knaw.huygens.timbuctoo.storage.graph;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
