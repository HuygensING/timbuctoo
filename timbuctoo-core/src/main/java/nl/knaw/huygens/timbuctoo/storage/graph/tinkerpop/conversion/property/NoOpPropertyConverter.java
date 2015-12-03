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

class NoOpPropertyConverter implements PropertyConverter {

  private String fieldName;
  private FieldType fieldType;
  private Class<? extends Entity> type;

  @Override
  public void setField(Field field) {}

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
    return this.fieldType;
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
  public void setPropertyOfElement(Element element, Entity entity) throws ConversionException {}

  @Override
  public void addValueToEntity(Entity entity, Element element) throws ConversionException {}

  @Override
  public String completePropertyName() {
    return fieldType.completePropertyName(type, fieldName);
  }

  @Override
  public void setPropertyName(String propertyName) {

  }

  @Override
  public void removeFrom(Element element) {}

}
