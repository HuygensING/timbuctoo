package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.JsonSerialization;

import javax.ws.rs.Produces;

@Produces(JsonWriter.MIME_TYPE)
public class JsonWriter extends SerializerWriter {

  public static final String MIME_TYPE = "application/json";

  public JsonWriter() {
    super(JsonSerialization::new);
  }

  @Override
  public String getMimeType() {
    return MIME_TYPE;
  }
}
