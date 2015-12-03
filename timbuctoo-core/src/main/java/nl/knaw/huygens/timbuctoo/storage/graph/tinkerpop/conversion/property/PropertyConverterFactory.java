package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

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

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.PropertyConverter;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.VIRTUAL;

public class PropertyConverterFactory {

  private final PropertyBusinessRules propertyBusinessRules;

  public PropertyConverterFactory() {
    this(new PropertyBusinessRules());
  }

  public PropertyConverterFactory(PropertyBusinessRules propertyBusinessRules) {
    this.propertyBusinessRules = propertyBusinessRules;
  }

  public <T extends Entity> PropertyConverter createPropertyConverter(Class<T> type, Field field) {
    FieldType fieldType = propertyBusinessRules.getFieldType(type, field);
    PropertyConverter propertyConverter = createPropertyConverter(field, fieldType);

    propertyConverter.setField(field);
    propertyConverter.setContainingType(type);
    propertyConverter.setFieldType(fieldType);
    propertyConverter.setFieldName(propertyBusinessRules.getFieldName(type, field));
    propertyConverter.setPropertyName(propertyBusinessRules.getPropertyName(type, field));

    return propertyConverter;
  }

  private PropertyConverter createPropertyConverter(Field field, FieldType fieldType) {
    if (fieldType == VIRTUAL) {
      return createNoOpPropertyConverter();
    }
     else if(isCollection(field)) {
      if (isSimpleCollection(field)) {
        return createSimpleCollectionPropertyConverter(getComponentType(field));
      }
      return createObjectCollectionPropertyConverter(getComponentType(field));
    } else if (isSimpleValue(field)) {
      return createSimpleValuePropertyConverter();
    }


    return createObjectValuePropertyConverter();
  }

  private Class<?> getComponentType(Field field) {
    Type[] actualTypeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

    if (actualTypeArguments.length <= 0) {
      return null;
    }

    Type firstTypeArgument = actualTypeArguments[0];
    return firstTypeArgument instanceof Class<?> ? (Class<?>) firstTypeArgument : null;

  }

  private boolean isSimpleCollection(Field field) {
    return isCollection(field) && hasSimpleTypeArgument(field);
  }

  private boolean isCollection(Field field) {
    return Collection.class.isAssignableFrom(field.getType());
  }

  private boolean hasSimpleTypeArgument(Field field) {
    Class<?> componentType = getComponentType(field);

    if (componentType == null) {
      return false;
    }

    return componentType instanceof Class ? isSimpleValueType(componentType) : false;
  }

  private boolean isSimpleValue(Field field) {
    Class<?> type = field.getType();

    return isSimpleValueType(type);
  }

  private boolean isSimpleValueType(Class<?> type) {
    return ClassUtils.isPrimitiveOrWrapper(type) || type == String.class;
  }

  protected PropertyConverter createSimpleValuePropertyConverter() {
    return new SimpleValuePropertyConverter();
  }

  protected PropertyConverter createObjectValuePropertyConverter() {
    return new ObjectValuePropertyConverter();
  }

  protected <T> PropertyConverter createObjectCollectionPropertyConverter(Class<T> componentType) {
    return new ObjectCollectionPropertyConverter(componentType);
  }

  protected PropertyConverter createNoOpPropertyConverter() {
    return new NoOpPropertyConverter();
  }

  protected <T> PropertyConverter createSimpleCollectionPropertyConverter(Class<T> componentType) {
    return new SimpleCollectionPropertyConverter<T>(componentType);
  }

}
