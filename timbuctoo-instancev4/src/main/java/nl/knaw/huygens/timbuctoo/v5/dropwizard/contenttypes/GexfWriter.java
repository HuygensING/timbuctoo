package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.GexfSerialization;

import javax.ws.rs.Produces;

@Produces(GexfWriter.MIME_TYPE)
public class GexfWriter extends SerializerWriter {

  public static final String MIME_TYPE = "application/gexf+xml";

  public GexfWriter() {
    super(GexfSerialization::new);
  }

  @Override
  public String getMimeType() {
    return MIME_TYPE;
  }
}
