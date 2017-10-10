package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.GraphMlSerialization;
import javax.ws.rs.Produces;

@Produces(GraphMlWriter.MIME_TYPE)
public class GraphMlWriter extends SerializerWriter {

  public static final String MIME_TYPE = "text/graphml";

  public GraphMlWriter() {
    super(GraphMlSerialization::new);
  }

  @Override
  public String getMimeType() {
    return MIME_TYPE;
  }
}
