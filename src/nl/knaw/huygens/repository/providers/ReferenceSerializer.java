package nl.knaw.huygens.repository.providers;

import java.io.IOException;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.Reference;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ReferenceSerializer extends StdSerializer<Reference> {

  private static final String SEMICOLON = "&#59;";

  private final DocTypeRegistry registry;

  public ReferenceSerializer(DocTypeRegistry registry) {
    super(Reference.class);
    this.registry = registry;
  }

  @Override
  public void serialize(Reference value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeRaw(createHTML(value));
  }

  private String createHTML(Reference reference) {
    Class<? extends Document> type = reference.getType();
    String variation = reference.getVariation();

    StringBuilder builder = new StringBuilder("<a href=\"");
    builder.append(registry.getINameForType(type));
    builder.append('/');
    builder.append(reference.getId());
    if (StringUtils.isNotBlank(variation)) {
      builder.append('/').append(variation);
    }
    builder.append("\">");
    builder.append(registry.getINameForType(type));
    if (StringUtils.isNotBlank(variation)) {
      builder.append(" (").append(variation).append(")");
    }
    builder.append("</a>" + SEMICOLON + "<br>\n");

    return builder.toString();
  }

}
