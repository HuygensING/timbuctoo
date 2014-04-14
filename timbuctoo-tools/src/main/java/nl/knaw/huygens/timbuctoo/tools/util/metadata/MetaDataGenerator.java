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
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MetaDataGenerator {
  private final FieldMetaDataGeneratorFactory fieldMetaDataGeneratorFactory;

  public MetaDataGenerator(FieldMetaDataGeneratorFactory fieldMetaDataGeneratorFactory) {
    this.fieldMetaDataGeneratorFactory = fieldMetaDataGeneratorFactory;
  }

  public Map<String, Object> generate(Class<?> type) {
    Map<String, Object> metaDataMap = createMetaDataMap();

    TypeFacade typeFacade = createTypeFacade(type);

    if (!isAbstract(type)) {
      for (Field field : getFields(type)) {
        FieldMetaDataGenerator fieldMetaDataGenerator = fieldMetaDataGeneratorFactory.create(field, typeFacade);

        fieldMetaDataGenerator.addMetaDataToMap(metaDataMap, field);

      }
    }

    return metaDataMap;
  }

  protected TypeFacade createTypeFacade(Class<?> type) {
    return new TypeFacade(type);
  }

  protected Map<String, Object> createMetaDataMap() {
    return Maps.newHashMap();
  }

  private List<Field> getFields(Class<?> type) {

    List<Field> fields = Lists.newArrayList(type.getDeclaredFields());

    if (!Object.class.equals(type.getSuperclass())) {
      fields.addAll(getFields(type.getSuperclass()));
    }

    return fields;
  }

  private boolean isAbstract(Class<?> type) {
    return Modifier.isAbstract(type.getModifiers()) || type.isInterface();
  }

}