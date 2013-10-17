package nl.knaw.huygens.timbuctoo.rest.providers;

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
