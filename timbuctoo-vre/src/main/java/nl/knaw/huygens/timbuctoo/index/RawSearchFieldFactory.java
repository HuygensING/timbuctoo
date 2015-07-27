package nl.knaw.huygens.timbuctoo.index;

/*
 * #%L
 * Timbuctoo VRE
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

import nl.knaw.huygens.timbuctoo.annotations.RawSearchField;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;

public class RawSearchFieldFactory {

  public String getRawSearchField(Class<? extends DomainEntity> type) {
    RawSearchField annotation = getAnnotation(type);

    if (annotation != null) {
      return annotation.value();
    }

    return "";
  }

  private RawSearchField getAnnotation(Class<? extends DomainEntity> type) {
    Class<?> typeToGetAnnotationFrom = type;
    for (; Entity.class.isAssignableFrom(typeToGetAnnotationFrom);) {
      RawSearchField annotation = typeToGetAnnotationFrom.getAnnotation(RawSearchField.class);
      if (annotation != null) {
        return annotation;
      }
      typeToGetAnnotationFrom = typeToGetAnnotationFrom.getSuperclass();
    }

    return null;
  }
}
