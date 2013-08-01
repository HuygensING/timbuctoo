package nl.knaw.huygens.repository.providers;

import java.io.IOException;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Reference;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ReferenceSerializer extends StdSerializer<Reference> {

  private final DocTypeRegistry registry;

  public ReferenceSerializer(DocTypeRegistry registry) {
    super(Reference.class);
    this.registry = registry;
  }

  @Override
  public void serialize(Reference reference, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
    String name = registry.getXNameForType(reference.getType());
    generator.writeRaw("<a href=\"");
    generator.writeRaw(name);
    generator.writeRaw("/");
    generator.writeRaw(reference.getId());
    generator.writeRaw("\">");
    generator.writeRaw(name);
    generator.writeRaw("</a><br/>");
  }

}
