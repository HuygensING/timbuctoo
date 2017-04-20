package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.JsonLdSerialization;

import javax.ws.rs.Produces;

@Produces("application/ld+json")
public class JsonLdWriter extends SerializerWriter {
  public JsonLdWriter() {
    super(JsonLdSerialization::new);
  }
}
