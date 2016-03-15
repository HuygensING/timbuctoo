package nl.knaw.huygens.timbuctoo.storage;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;

public interface Properties {

  /**
   * Returns the prefix for a property name.
   * @param type the type token of the entity.
   * @return The prefix.
   */
  String propertyPrefix(Class<?> type);

  /**
   * Creates the property name for a field of an entity.
   * @param type the type token of the entity.
   * @param fieldName the name of the field; must not be null or empty.
   * @return The property name.
   */
  String propertyName(Class<? extends Entity> type, String fieldName);

  /**
   * Creates the property name for a field of an entity.
   * @param prefix the prefix of the property name.
   * @param fieldName the name of the field; must not be null or empty.
   * @return The property name.
   */
  String propertyName(String prefix, String fieldName);

  /**
   * @return
   */
  ObjectNode createObjectNode();

  /**
   * @param type
   * @param value
   * @return
   * @throws StorageException
   */
  JsonNode induce(Class<?> type, Object value) throws StorageException;

  /**
   * Converts a json node from the storage to a property value.
   * @param field the field to reduce the value for.
   * @param node the json node to transform.
   * @return the property value.
   * @throws StorageException Thrown if conversion fails.
   */
  Object reduce(Field field, JsonNode node) throws StorageException;

}
