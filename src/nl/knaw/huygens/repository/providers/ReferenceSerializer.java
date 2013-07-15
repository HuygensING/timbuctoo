package nl.knaw.huygens.repository.providers;

import java.io.IOException;

import nl.knaw.huygens.repository.model.Reference;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ReferenceSerializer extends StdSerializer<Reference> {

  public ReferenceSerializer() {
    super(Reference.class);
  }

  @Override
  public void serialize(Reference value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeRaw(createHTML(value));
  }

  private String createHTML(Reference reference) {
    StringBuilder sb = new StringBuilder("<a href=\"");
    sb.append(reference.getType().getSimpleName().toLowerCase());
    sb.append('/');
    sb.append(reference.getId());
    if (reference.getVariation() != null) {
      sb.append('/').append(reference.getVariation());
    }
    sb.append("\">");
    sb.append(reference.getLinkName());
    //&#59; is the escape character for ';'
    sb.append("</a>&#59;<br>\n");

    return sb.toString();
  }
}
