package nl.knaw.huygens.timbuctoo.tools.util;

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

import java.io.IOException;
import java.io.PrintWriter;

import nl.knaw.huygens.timbuctoo.annotations.JsonViews;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Utility class for converting entities to JSON without type information
 * and administrative properties.
 */
public class EntityToJsonConverter {

  /** Regular expression that matches type info of entities. */
  private static final String TYPE_INFO = "\"@type\":\"\\w+\",";

  private final ObjectWriter writer;

  public EntityToJsonConverter() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
    writer = mapper.writerWithView(JsonViews.ExportView.class);
  }

  public <T extends Entity> String convert(T entity) throws IOException {
    return writer.writeValueAsString(entity).replaceAll(TYPE_INFO, "");
  }

  public <T extends Entity> void appendTo(PrintWriter writer, T entity) throws IOException {
    if (entity != null) {
      writer.println(convert(entity));
    }
  }

}
