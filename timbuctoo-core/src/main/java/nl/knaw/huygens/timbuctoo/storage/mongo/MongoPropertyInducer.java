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

import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.storage.PropertyInducer;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MongoPropertyInducer implements PropertyInducer {

  private final ObjectMapper jsonMapper;

  public MongoPropertyInducer() {
    jsonMapper = new ObjectMapper();
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

}
