package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class CollectionMapperTest {

  public static final String VRE_NAME = "vreName";
  private GraphWrapper graphWrapper;

  @Before
  public void setUp() throws Exception {
    graphWrapper = newGraph()
      .withVertex(v -> {
        v.withLabel(Vre.DATABASE_LABEL);
        v.withProperty(Vre.VRE_NAME_PROPERTY_NAME, VRE_NAME);
      })
      .wrap();
  }

  @Test
  public void addToCollectionCreatesACollectionVertexWithAnEntityTypeNameAndCollectionName() {
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    instance.addToCollection(graph.addVertex(), new CollectionDescription("test", VRE_NAME));

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL).next(),
      is(likeVertex().withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "test")
                     .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "tests")));
  }


  @Test
  public void addToCollectionAddsOneEntityNodeToTheCollection() {
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    instance.addToCollection(graph.addVertex(), new CollectionDescription("test", VRE_NAME));

    assertThat(graph.traversal().V()
                    .hasLabel(Collection.DATABASE_LABEL).out(Collection.HAS_ENTITY_NODE_RELATION_NAME).count().next(),
      is(1L));
  }

  @Test
  public void addToCollectionAddsTheCollectionToTheRequestedVre() {
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    instance.addToCollection(graph.addVertex(), new CollectionDescription("test", VRE_NAME));

    assertThat(graph.traversal().V().has(Vre.VRE_NAME_PROPERTY_NAME).out(Vre.HAS_COLLECTION_RELATION_NAME).hasNext(),
      is(true)
    );
    assertThat(graph.traversal().V().has(Vre.VRE_NAME_PROPERTY_NAME, VRE_NAME)
                    .out(Vre.HAS_COLLECTION_RELATION_NAME).next(), likeVertex()
      .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "tests")
      .withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "test")
    );
  }

  @Test
  public void addToCollectionAddsTheVertexToTheEntityNode() {
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();
    Vertex vertexToAdd = graph.addVertex();

    instance.addToCollection(vertexToAdd, new CollectionDescription("test", VRE_NAME));

    assertThat(graph.traversal().V()
                    .hasLabel(Collection.DATABASE_LABEL)
                    .out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).next(),
      is(vertexToAdd));
  }

  @Test
  public void addToCollectionReusesAnExistingCollectionWithTheSameEntityTypeName() {
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    instance.addToCollection(graph.addVertex(), new CollectionDescription("test", VRE_NAME));
    instance.addToCollection(graph.addVertex(), new CollectionDescription("test", VRE_NAME));

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL).out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(2L));
  }

  @Test
  public void addToCollectionDoesNotConnectAVertexMoreThanOnce() {
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    Vertex vertex = graph.addVertex();
    instance.addToCollection(vertex, new CollectionDescription("test", VRE_NAME));
    instance.addToCollection(vertex, new CollectionDescription("test", VRE_NAME));

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL).out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(1L));
  }

  @Test
  public void addToCollectionChangesTheCollectionIfThePreviousCollectionWasUnknown() {
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    Vertex vertex = graph.addVertex();
    instance.addToCollection(vertex, new CollectionDescription("unknown", VRE_NAME));


    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL)
                    .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "unknown")
                    .out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(1L));
    instance.addToCollection(vertex, new CollectionDescription("test", VRE_NAME));

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL)
                    .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "test")
                    .out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(1L));
    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL)
                    .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "unknown")
                    .out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(0L));
  }

  @Test
  public void addToCollectionDoesNotAddToUnknownIfVertexIsPartOfAKnownCollection() {
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    Vertex vertex = graph.addVertex();
    instance.addToCollection(vertex, new CollectionDescription("test", VRE_NAME));
    instance.addToCollection(vertex, new CollectionDescription("unknown", VRE_NAME));

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL)
                    .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "test")
                    .out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(1L));
    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL)
                    .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "unknown")
                    .out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(0L));
  }

  @Test
  public void getCollectionDescriptionsReturnsTheCollectionDescriptionsOfTheVertex() {
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();
    Vertex vertex = graph.addVertex();
    instance.addToCollection(vertex, new CollectionDescription("test", VRE_NAME));
    instance.addToCollection(vertex, new CollectionDescription("unknown", VRE_NAME));
    instance.addToCollection(vertex, new CollectionDescription("other", VRE_NAME));

    final List<String> result = instance
      .getCollectionDescriptions(vertex, VRE_NAME).stream()
      .map(CollectionDescription::getEntityTypeName)
      .collect(Collectors.toList());

    assertThat(result, containsInAnyOrder("test", "other"));
  }
}
