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

import com.google.common.collect.Maps;

public class ConstantFieldMetaDataGenerator extends FieldMetaDataGenerator {

  public ConstantFieldMetaDataGenerator(TypeFacade containingType) {
    super(containingType);
  }

  @Override
  protected Map<String, Object> constructValue(Field field) {
    Map<String, Object> metaDataMap = Maps.newHashMap();
    metaDataMap.put(TYPE_FIELD, containingType.getTypeNameOfField(field));
    // to get the values of private constants
    field.setAccessible(true);

    try {
      metaDataMap.put(VALUE_FIELD, field.get(null));
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return metaDataMap;
  }
}
