package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

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

import java.lang.reflect.Field;

public interface PropertyConverter {

  void setField(Field field);

  void setContainingType(Class<? extends Entity> type);

  void setFieldType(FieldType fieldType);

  void setFieldName(String fieldName);

  void setPropertyOfElement(Element element, Entity entity) throws ConversionException;

  void addValueToEntity(Entity entity, Element element) throws ConversionException;

  /**
   * Creates the name of the property with potential prefixes.
   *
   * @return the property name
   */
  String completePropertyName();

  /**
   * Set the completePropertyName.
   *
   * @param propertyName the property name without potential prefixes.
   */
  void setPropertyName(String propertyName);

  String getFieldName();

  FieldType getFieldType();

  /**
   * Removes the property from the element.
   *
   * @param element the element to remove the property from
   */
  void removeFrom(Element element);


}
