package nl.knaw.huygens.timbuctoo.storage;

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

import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;
import nl.knaw.huygens.timbuctoo.model.Entity;

public class XProperties {

  /**
   * Creates the property name for a field of an entity.
   * @param type the type token of the entity.
   * @param fieldName the name of the field; must not be null or empty.
   * @return The property name.
   */
  public static String propertyName(Class<? extends Entity> type, String fieldName) {
    return Character.isLetter(fieldName.charAt(0)) ? getInternalName(type) + ":" + fieldName : fieldName;
  }

}
