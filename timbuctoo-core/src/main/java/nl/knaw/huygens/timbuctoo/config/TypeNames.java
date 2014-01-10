package nl.knaw.huygens.timbuctoo.config;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.model.Entity;

/**
 * A utility class to get the internal and external name of a class.
 * The "internal name" is used in the database, Solr index, etc.
 * The "external name" is used in REST service.  
 */
public class TypeNames {

  public static String getInternalName(Class<?> type) {
    return type.getSimpleName().toLowerCase();
  }

  public static String getExternalName(Class<? extends Entity> type) {
    if (type.isAnnotationPresent(EntityTypeName.class)) {
      return type.getAnnotation(EntityTypeName.class).value();
    } else {
      return getInternalName(type) + "s";
    }
  }

}
