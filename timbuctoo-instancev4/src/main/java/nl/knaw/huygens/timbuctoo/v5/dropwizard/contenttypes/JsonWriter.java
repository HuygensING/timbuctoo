package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.JsonSerialization;

import javax.ws.rs.Produces;

@Produces("application/json")
public class JsonWriter extends SerializerWriter {
  public JsonWriter() {
    super(JsonSerialization::new);
  }
}
