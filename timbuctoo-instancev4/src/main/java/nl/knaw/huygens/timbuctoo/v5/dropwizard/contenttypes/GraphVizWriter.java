package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.GraphVizSerialization;

import javax.ws.rs.Produces;

@Produces("text/vnd.graphviz")
public class GraphVizWriter extends SerializerWriter {
  public GraphVizWriter() {
    super(GraphVizSerialization::new);
  }
}
