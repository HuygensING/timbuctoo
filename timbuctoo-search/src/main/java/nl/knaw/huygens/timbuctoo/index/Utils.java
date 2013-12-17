package nl.knaw.huygens.timbuctoo.index;

/*
 * #%L
 * Timbuctoo search
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

/**
 * Contains various index-related utility methods.
 */
public class Utils {

  /**
   * Determines the index field name from the method name (only used if the
   * annotation doesn't specify a fieldname).
   * 
   * @param method
   *          the Method object for which a Solr field name should be generated.
   * @return the field name
   */
  public static String getFieldName(Method method) {
    String name = method.getName();
    String type = method.getReturnType().getSimpleName();
    String rv = name.startsWith("get") ? name.substring(3) : name; // eliminate
                                                                   // 'get' part
    String[] parts = StringUtils.splitByCharacterTypeCamelCase(rv);
    type = type.replaceAll("\\[\\]", "");
    if (type.equals("boolean")) {
      type = "b";
    } else if (type.equals("int") || type.equals("long")) {
      type = "i";
    } else {
      type = "s";
    }
    return "dynamic_" + type + "_" + StringUtils.join(parts, "_").toLowerCase();
  }

  private Utils() {
    throw new AssertionError("Non-instantiable class");
  }

}
