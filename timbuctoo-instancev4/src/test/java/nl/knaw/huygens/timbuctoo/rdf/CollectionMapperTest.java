package nl.knaw.huygens.timbuctoo.rdf;

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

    instance.addToCollection(graph.addVertex(), "test");

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL).next(),
      is(likeVertex().withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "test")
                     .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "tests")));
  }

  @Test
  public void addToCollectionAddsOneEntityNodeToTheCollection() {
    GraphWrapper graphWrapper = newGraph().wrap();
    CollectionMapper instance = new CollectionMapper(graphWrapper);
    Graph graph = graphWrapper.getGraph();

    instance.addToCollection(graph.addVertex(), "test");

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

    instance.addToCollection(vertexToAdd, "test");

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

    instance.addToCollection(graph.addVertex(), "test");
    instance.addToCollection(graph.addVertex(), "test");

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL).out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(2L));
  }

}
