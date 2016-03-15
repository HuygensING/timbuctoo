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

import com.tinkerpop.blueprints.Element;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.PropertyConverter;

import java.lang.reflect.Field;

abstract class AbstractPropertyConverter implements PropertyConverter {
  private Field field;
  private Class<? extends Entity> type;
  private FieldType fieldType;
  private String fieldName;
  private String propertyName;

  @Override
  public void setField(Field field) {
    this.field = field;
  }

  @Override
  public void setContainingType(Class<? extends Entity> type) {
    this.type = type;
  }

  @Override
  public void setFieldType(FieldType fieldType) {
    this.fieldType = fieldType;
  }

  @Override
  public FieldType getFieldType() {
    return fieldType;
  }

  @Override
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  @Override
  public final void setPropertyOfElement(Element element, Entity entity) throws ConversionException {
    try {
      Object value = getValue(entity);

      String propertyName = completePropertyName();
      if (isLegalValue(value)) {
        element.setProperty(propertyName, format(value));
      } else {
        element.removeProperty(propertyName);
      }

    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new ConversionException(e);
    }
  }

  protected abstract Object format(Object value) throws IllegalArgumentException;

  protected boolean isLegalValue(Object value) {
    return value != null;
  }

  @Override
  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  @Override
  public String completePropertyName() {
    return fieldType.completePropertyName(type, propertyName);
  }

  protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
    field.setAccessible(true);
    return field.get(entity);
  }

  @Override
  public final void addValueToEntity(Entity entity, Element element) throws ConversionException {
    Object value = element.getProperty(completePropertyName());

    try {
      fillField(entity, value);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new ConversionException(e);
    }
  }

  protected abstract Object convert(Object value, Class<?> fieldType);

  protected void fillField(Entity entity, Object value) throws IllegalAccessException, IllegalArgumentException {
    if (value != null) {
      field.setAccessible(true);
      Object convertedValue = convert(value, field.getType());
      field.set(entity, convertedValue);
    }
  }

  @Override
  public void removeFrom(Element element) {
    element.removeProperty(completePropertyName());
  }
}
