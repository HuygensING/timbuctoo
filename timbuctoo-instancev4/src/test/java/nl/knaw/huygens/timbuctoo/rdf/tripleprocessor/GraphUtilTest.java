package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
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
  public static final String TEST_URI = "http://www.example.com/node";
  private GraphWrapper graphWrapper;
  private Node node;
  private SystemPropertyModifier modifier;


  @Before
  public void setUp() throws Exception {
    graphWrapper = newGraph().wrap();
    node = mock(Node.class);
    when(node.getURI()).thenReturn(TEST_URI);
    modifier = mock(SystemPropertyModifier.class);
  }

  @Test
  public void findOrCreateEntityVertexCreateANewVertexWithTimbuctoosSystemProperties() {
    final CollectionDescription collectionDescription = CollectionDescription.getDefault(null);
    final CollectionMapper collectionMapper = mock(CollectionMapper.class);
    final GraphUtil instance = new GraphUtil(graphWrapper, modifier, collectionMapper);

    Vertex vertex = instance.findOrCreateEntityVertex(node, collectionDescription);

    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP).next(), is(vertex));
    verify(modifier).setCreated(vertex, USER_ID);
    verify(modifier).setModified(vertex, USER_ID);
    verify(modifier).setTimId(vertex);
    verify(modifier).setRev(vertex, 1);
    verify(modifier).setIsLatest(vertex, true);
    verify(modifier).setIsDeleted(vertex, false);
  }

  @Test
  public void findOrCreateEntityVertexAddsANewlyCreatedEntityToTheDefaultCollection() {
    final CollectionMapper collectionMapper = mock(CollectionMapper.class);
    final GraphUtil instance = new GraphUtil(graphWrapper, modifier, collectionMapper);
    final CollectionDescription collectionDescription = CollectionDescription.getDefault(null);

    Vertex vertex = instance.findOrCreateEntityVertex(node, collectionDescription);

    verify(collectionMapper).addToCollection(vertex, collectionDescription);
  }

  @Test
  public void findOrCreateEntityVertexAddsANewlyCreatedEntityToTheRequestedCollection() {
    final CollectionDescription requestedCollection = new CollectionDescription("requested", null);
    final CollectionMapper collectionMapper = mock(CollectionMapper.class);
    final GraphUtil instance = new GraphUtil(graphWrapper, modifier, collectionMapper);

    Vertex vertex = instance.findOrCreateEntityVertex(node, requestedCollection);

    verify(collectionMapper).addToCollection(vertex, requestedCollection);
  }

  @Test
  public void findOrCreateEntityVertexSetsTheCollectionForAnExistingVertex() {
    final GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v.withProperty(RDF_URI_PROP, TEST_URI)).wrap();

    final CollectionDescription requestedCollection = new CollectionDescription("requested", null);
    final CollectionMapper collectionMapper = mock(CollectionMapper.class);
    final GraphUtil instance = new GraphUtil(graphWrapper, modifier, collectionMapper);

    Vertex vertex = graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, TEST_URI).next();

    instance.findOrCreateEntityVertex(node, requestedCollection);
    verify(collectionMapper).addToCollection(vertex, requestedCollection);

  }
}
