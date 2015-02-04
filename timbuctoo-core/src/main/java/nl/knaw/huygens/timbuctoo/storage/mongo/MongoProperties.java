package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
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

import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.storage.Properties;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MongoProperties implements Properties {

  /** Separator between parts of a property name. */
  private static final String SEPARATOR = ":";

  private final ObjectMapper jsonMapper;

  public MongoProperties() {
    jsonMapper = new ObjectMapper();
  }

  @Override
  public String propertyPrefix(Class<?> type) {
    return TypeNames.getInternalName(type);
  }

  @Override
  public String propertyName(Class<? extends Entity> type, String fieldName) {
    return propertyName(propertyPrefix(type), fieldName);
  }

  @Override
  public String propertyName(String prefix, String fieldName) {
    return Character.isLetter(fieldName.charAt(0)) ? prefix + SEPARATOR + fieldName : fieldName;
  }

  @Override
  public ObjectNode createObjectNode() {
    return jsonMapper.createObjectNode();
  }

  @Override
  public JsonNode induce(Class<?> type, Object value) throws StorageException {
    Object converted = convert(type, value);
    return jsonMapper.valueToTree(converted);
  }

  private Object convert(Class<?> type, Object value) {
    if (value == null) {
      return null;
    } else if (type == Datable.class) {
      return Datable.class.cast(value).getEDTF();
    } else if (Collection.class.isAssignableFrom(type)) {
      Collection<?> collection = Collection.class.cast(value);
      if (collection.isEmpty()) {
        return null;
      }
    }
    return value;
  }

  @Override
  public Object reduce(Class<?> type, JsonNode node) throws StorageException {
    if (node.isArray()) {
      return createCollection(node);
    } else if (type == Integer.class || type == int.class) {
      return node.asInt();
    } else if (type == Boolean.class || type == boolean.class) {
      return node.asBoolean();
    } else if (type == Character.class || type == char.class) {
      return node.asText().charAt(0);
    } else if (type == Double.class || type == double.class) {
      return node.asDouble();
    } else if (type == Float.class || type == float.class) {
      return (float) node.asDouble();
    } else if (type == Long.class || type == long.class) {
      return node.asLong();
    } else if (type == Short.class || type == short.class) {
      return (short) node.asInt();
    } else if (Datable.class.isAssignableFrom(type)) {
      return new Datable(node.asText());
    } else {
      return jsonMapper.convertValue(node, type);
    }
  }

  private Object createCollection(JsonNode value) throws StorageException {
    try {
      return jsonMapper.readValue(value.toString(), new TypeReference<List<? extends Object>>() {});
    } catch (Exception e) {
      throw new StorageException(e);
    }
  }

}
