package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.GraphUtil.RDF_URI_PROP;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GraphUtilTest {

  public static final String USER_ID = "rdf-importer";

  @Test
  public void findOrCreateEntityVertexCreateANewVertexWithTimbuctoosSystemProperties() {
    GraphWrapper graphWrapper = newGraph().wrap();
    Node node = mock(Node.class);
    when(node.getURI()).thenReturn("http://www.example.com/node");

    SystemPropertyModifier modifier = mock(SystemPropertyModifier.class);
    Vertex vertex = new GraphUtil(graphWrapper, modifier).findOrCreateEntityVertex(node);

    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP).next(), is(vertex));
    verify(modifier).setCreated(vertex, USER_ID);
    verify(modifier).setModified(vertex, USER_ID);
    verify(modifier).setTimId(vertex);
    verify(modifier).setRev(vertex, 1);
    verify(modifier).setIsLatest(vertex, true);
  }
}
