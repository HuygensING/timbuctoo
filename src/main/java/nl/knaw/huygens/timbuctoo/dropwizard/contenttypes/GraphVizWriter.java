package nl.knaw.huygens.timbuctoo.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.serializable.serializations.GraphVizSerialization;

import javax.ws.rs.Produces;

@Produces(GraphVizWriter.MIME_TYPE)
public class GraphVizWriter extends SerializerWriter {

  public static final String MIME_TYPE = "text/vnd.graphviz";

  public GraphVizWriter() {
    super(GraphVizSerialization::new);
  }

  @Override
  public String getMimeType() {
    return MIME_TYPE;
  }
}
