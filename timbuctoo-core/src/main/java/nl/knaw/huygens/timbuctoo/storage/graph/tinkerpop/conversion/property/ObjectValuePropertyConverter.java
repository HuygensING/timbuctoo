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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class ObjectValuePropertyConverter extends AbstractPropertyConverter {

  private final ObjectMapper objectMapper;

  public ObjectValuePropertyConverter() {
    objectMapper = new ObjectMapper();
  }

  @Override
  protected Object format(Object value) throws IllegalArgumentException {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  protected Object convert(Object value, Class<?> fieldType) {
    if (!(value instanceof String)) {
      throw new IllegalArgumentException("Value should be a String");
    }
    try {
      return objectMapper.readValue(value.toString(), fieldType);
    } catch (IOException e) {
      throw new IllegalArgumentException("Value could not be read.");
    }
  }

}
