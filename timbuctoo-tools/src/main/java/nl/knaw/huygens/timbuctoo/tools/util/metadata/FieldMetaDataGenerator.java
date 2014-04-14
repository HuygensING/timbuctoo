package nl.knaw.huygens.timbuctoo.tools.util.metadata;

/*
 * #%L
 * Timbuctoo tools
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

import java.lang.reflect.Field;
import java.util.Map;

public abstract class FieldMetaDataGenerator {

  protected static final String TYPE_FIELD = "type";
  protected static final String VALUE_FIELD = "value";
  protected final TypeFacade containingType;

  public FieldMetaDataGenerator(TypeFacade containingType) {
    this.containingType = containingType;

  }

  /**
   * Add the metadata to the map of the {@code containingType}
   * @param mapToAddTo
   * @param field the field to get the metadata from.
   */
  public void addMetaDataToMap(Map<String, Object> mapToAddTo, Field field) {

    Map<String, Object> value = constructValue(field);

    mapToAddTo.put(containingType.getFieldName(field), value);
  }

  protected abstract Map<String, Object> constructValue(Field field);
}
