package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CollectionMapperTest {
  @Test
  public void addToCollectionCreatesACollectionVertexWithAnEntityTypeNameAndCollectionName() {
    GraphWrapper graphWrapper = newGraph().wrap();
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    instance.addToCollection(graph.addVertex(), new CollectionDescription("test"));

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL).next(),
      is(likeVertex().withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "test")
                     .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "tests")));
  }

  @Test
  public void addToCollectionAddsOneEntityNodeToTheCollection() {
    GraphWrapper graphWrapper = newGraph().wrap();
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    instance.addToCollection(graph.addVertex(), new CollectionDescription("test"));

    assertThat(graph.traversal().V()
                    .hasLabel(Collection.DATABASE_LABEL).out(Collection.HAS_ENTITY_NODE_RELATION_NAME).count().next(),
      is(1L));
  }

  @Test
  public void addToCollectionAddsTheVertexToTheEntityNode() {
    GraphWrapper graphWrapper = newGraph().wrap();
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();
    Vertex vertexToAdd = graph.addVertex();

    instance.addToCollection(vertexToAdd, new CollectionDescription("test"));

    assertThat(graph.traversal().V()
                    .hasLabel(Collection.DATABASE_LABEL)
                    .out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).next(),
      is(vertexToAdd));
  }

  @Test
  public void addToCollectionReusesAnExistingCollectionWithTheSameEntityTypeName() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    instance.addToCollection(graph.addVertex(), new CollectionDescription("test"));
    instance.addToCollection(graph.addVertex(), new CollectionDescription("test"));

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL).out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(2L));
  }

  @Test
  public void addToCollectionDoesNotConnectAVertexMoreThanOnce() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    Vertex vertex = graph.addVertex();
    instance.addToCollection(vertex, new CollectionDescription("test"));
    instance.addToCollection(vertex, new CollectionDescription("test"));

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL).out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(1L));
  }

  @Test
  public void addToCollectionChangesTheCollectionIfThePreviousCollectionWasUnknown() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    Vertex vertex = graph.addVertex();
    instance.addToCollection(vertex, new CollectionDescription("unknown"));
    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL)
                    .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "unknown")
                    .out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(1L));
    instance.addToCollection(vertex, new CollectionDescription("test"));

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
    final GraphWrapper graphWrapper = newGraph().wrap();
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    Vertex vertex = graph.addVertex();
    instance.addToCollection(vertex, new CollectionDescription("test"));
    instance.addToCollection(vertex, new CollectionDescription("unknown"));

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

}
