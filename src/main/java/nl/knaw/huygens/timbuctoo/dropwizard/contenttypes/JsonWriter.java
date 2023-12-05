package nl.knaw.huygens.timbuctoo.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.serializable.serializations.JsonLdSerialization;

import javax.ws.rs.Produces;

@Produces(JsonWriter.MIME_TYPE)
public class JsonWriter extends SerializerWriter {

  public static final String MIME_TYPE = "application/json";

  public JsonWriter() {
    super(JsonLdSerialization::new);
  }

  @Override
  public String getMimeType() {
    return MIME_TYPE;
  }
}
