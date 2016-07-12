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
  private GraphWrapper graphWrapper;
  private Node node;
  private SystemPropertyModifier modifier;


  @Before
  public void setUp() throws Exception {
    graphWrapper = newGraph().wrap();
    node = mock(Node.class);
    when(node.getURI()).thenReturn("http://www.example.com/node");
    modifier = mock(SystemPropertyModifier.class);
  }

  @Test
  public void findOrCreateEntityVertexCreateANewVertexWithTimbuctoosSystemProperties() {

    final CollectionDescription collectionDescription = CollectionDescription.getDefault(null);
    Vertex vertex =
      new GraphUtil(graphWrapper, modifier).findOrCreateEntityVertex(node, collectionDescription);

    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP).next(), is(vertex));
    verify(modifier).setCreated(vertex, USER_ID);
    verify(modifier).setModified(vertex, USER_ID);
    verify(modifier).setTimId(vertex);
    verify(modifier).setRev(vertex, 1);
    verify(modifier).setIsLatest(vertex, true);
    verify(modifier).setIsDeleted(vertex, false, collectionDescription);
  }

  @Test
  public void findOrCreateEntityVertexAddsANewlyCreatedEntityToTheDefaultCollection() {

    Vertex vertex =
      new GraphUtil(graphWrapper, modifier).findOrCreateEntityVertex(node, CollectionDescription.getDefault(null));

    assertThat(graphWrapper.getGraph().traversal().V(vertex.id())
                .in(Collection.HAS_ENTITY_RELATION_NAME)
                .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "unknown").hasNext(),
      is(true)
    );
  }

  @Test
  public void findOrCreateEntityVertexAddsANewlyCreatedEntityToTheRequestedCollection() {
    final CollectionDescription requestedCollection = new CollectionDescription("requested", null);

    Vertex vertex = new GraphUtil(graphWrapper, modifier).findOrCreateEntityVertex(node, requestedCollection);

    assertThat(graphWrapper.getGraph().traversal().V(vertex.id())
                           .in(Collection.HAS_ENTITY_RELATION_NAME)
                           .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                           .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "requested").hasNext(),
      is(true)
    );
    assertThat(graphWrapper.getGraph().traversal().V(vertex.id())
                           .in(Collection.HAS_ENTITY_RELATION_NAME)
                           .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                           .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "unknown").hasNext(),
      is(false)
    );
  }

  @Test
  public void findOrCreateEntityVertexReplacesTheDefaultCollectionWithTheRequestedCollection() {
    final CollectionDescription requestedCollection = new CollectionDescription("requested", null);
    final GraphUtil instance = new GraphUtil(graphWrapper, modifier);
    Vertex vertex = instance.findOrCreateEntityVertex(node, CollectionDescription.getDefault(null));

    instance.findOrCreateEntityVertex(node, requestedCollection);

    assertThat(graphWrapper.getGraph().traversal().V(vertex.id())
                           .in(Collection.HAS_ENTITY_RELATION_NAME)
                           .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                           .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "requested").hasNext(),
      is(true)
    );
    assertThat(graphWrapper.getGraph().traversal().V(vertex.id())
                           .in(Collection.HAS_ENTITY_RELATION_NAME)
                           .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                           .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "unknown").hasNext(),
      is(false)
    );
  }
}
