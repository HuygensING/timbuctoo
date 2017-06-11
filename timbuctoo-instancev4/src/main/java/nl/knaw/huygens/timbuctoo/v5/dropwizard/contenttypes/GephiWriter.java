package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.GephiGraphMlSerialization;

import javax.ws.rs.Produces;

@Produces("application/graphml+xml")
public class GephiWriter extends SerializerWriter {
  public GephiWriter() {
    super(GephiGraphMlSerialization::new);
  }
}
