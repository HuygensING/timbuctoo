package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.GephiGraphMlSerialization;

import javax.ws.rs.Produces;

@Produces(GephiWriter.MIME_TYPE)
public class GephiWriter extends SerializerWriter {

  public static final String MIME_TYPE = "application/graphml+xml";

  public GephiWriter() {
    super(GephiGraphMlSerialization::new);
  }

  @Override
  public String getMimeType() {
    return MIME_TYPE;
  }
}
