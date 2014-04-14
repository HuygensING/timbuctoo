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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EnumValueFieldMetaDataGenerator extends FieldMetaDataGenerator {

  public EnumValueFieldMetaDataGenerator(TypeFacade containingType) {
    super(containingType);
  }

  @Override
  protected Map<String, Object> constructValue(Field field) {
    Map<String, Object> metadataMap = Maps.newHashMap();
    metadataMap.put(TYPE_FIELD, getTypeName(field));

    addValueToValueMap(field, metadataMap);

    return metadataMap;
  }

  private String getTypeName(Field field) {
    if (List.class.isAssignableFrom(field.getType())) {
      return "List of (String)";
    }

    return "String";
  }

  protected void addValueToValueMap(Field field, Map<String, Object> metadataMap) {
    List<String> enumValues = Lists.newArrayList();
    Class<?> type = getEnumType(field);

    for (Object value : type.getEnumConstants()) {
      enumValues.add(value.toString());
    }
    metadataMap.put(VALUE_FIELD, enumValues);
  }

  private Class<?> getEnumType(Field field) {
    if (field.getType().isEnum()) {
      return field.getType();
    }

    for (Type paramType : ((ParameterizedType) field.getGenericType()).getActualTypeArguments()) {

      if (paramType instanceof Class<?>) {
        Class<?> paramClass = (Class<?>) paramType;
        if (paramClass.isEnum()) {
          return paramClass;
        }
      }
    }
    return null;
  }
}
