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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MethodHelper {
  static final String GET_ACCESSOR = "get";
  static final String IS_ACCESSOR = "is"; // get accesor for booleans.


  /**
   * Searches for a public method in the specified class or its superclasses
   * and -interfaces that matches the specified name and has no parameters.
   */
  public static Method getMethodByName(Class<?> type, String methodName) {
    try {
      // TODO decide: use type.getDeclaredMethod(methodName)?
      return type.getMethod(methodName);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  public static String getGetterName(Field field) {
    char[] fieldNameChars = field.getName().toCharArray();

    fieldNameChars[0] = Character.toUpperCase(fieldNameChars[0]);

    String accessor = isBoolean(field.getType()) ? IS_ACCESSOR : GET_ACCESSOR;
    return accessor.concat(String.valueOf(fieldNameChars));
  }

  private static boolean isBoolean(Class<?> cls) {
    return cls == boolean.class || cls == Boolean.class;
  }
}
