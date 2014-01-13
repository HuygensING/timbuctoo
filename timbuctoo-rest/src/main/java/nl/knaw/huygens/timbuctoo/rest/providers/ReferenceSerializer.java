package nl.knaw.huygens.timbuctoo.rest.providers;

/*
 * #%L
 * Timbuctoo REST api
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

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Reference;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ReferenceSerializer extends StdSerializer<Reference> {

  private final TypeRegistry registry;

  public ReferenceSerializer(TypeRegistry registry) {
    super(Reference.class);
    this.registry = registry;
  }

  @Override
  public void serialize(Reference reference, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
    String name = registry.getXNameForIName(reference.getType());
    generator.writeRaw("<a href=\"");
    generator.writeRaw(name);
    generator.writeRaw("/");
    generator.writeRaw(reference.getId());
    generator.writeRaw("\">");
    generator.writeRaw(name);
    generator.writeRaw("</a><br/>");
  }

}
