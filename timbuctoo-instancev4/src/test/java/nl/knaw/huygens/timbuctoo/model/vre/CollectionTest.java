package nl.knaw.huygens.timbuctoo.model.vre;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.model.vre.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.CollectionBuilder.timbuctooCollection;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class CollectionTest {
  private GraphWrapper graphWrapper;

  @Before
  public void setUp() {
    Graph graph = newGraph().build();
    this.graphWrapper = mock(GraphWrapper.class);
    given(graphWrapper.getGraph()).willReturn(graph);
  }


  @Test
  public void saveCreatesAVertexForTheCollection() {
    final Vre vre = new Vre("VreName");

    timbuctooCollection("persons", "").build(vre);

    final Vertex result = vre.getCollectionForCollectionName("persons").get().save(graphWrapper);

    assertThat(result, likeVertex()
      .withLabel(DATABASE_LABEL)
      .withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, "person")
      .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "persons")
    );

    // Unprefixed collection is considered an archetype
    assertThat(result.vertices(Direction.OUT, Collection.HAS_ARCHETYPE_RELATION_NAME).hasNext(), equalTo(false));
  }

  @Test
  public void saveReplacesAnExistingVertexForTheCollection() {
    final Vertex existingVertex = graphWrapper.getGraph().addVertex(DATABASE_LABEL);
    existingVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, "persons");

    final Vre vre = new Vre("VreName");

    timbuctooCollection("persons", "").build(vre);

    final Vertex result = vre.getCollectionForCollectionName("persons").get().save(graphWrapper);

    assertThat(result, equalTo(existingVertex));
  }

  @Test
  public void saveCreatesARelationToAnEntityHolderVertex() {
    final Vre vre = new Vre("VreName");

    timbuctooCollection("persons", "").build(vre);

    final Vertex result = vre.getCollectionForCollectionName("persons").get().save(graphWrapper);
    assertThat(result.vertices(Direction.OUT, Collection.HAS_ENTITY_NODE_RELATION_NAME).next(), likeVertex()
      .withLabel(Collection.COLLECTION_ENTITIES_LABEL)
    );
  }

  @Test
  public void saveDoesNotCreateARelationToAnEntityHolderVertexIfItAlreadyExists() {
    final Vre vre = new Vre("VreName");
    final Graph graph = graphWrapper.getGraph();
    final Vertex existingVertex = graph.addVertex(DATABASE_LABEL);
    final Vertex existingEntityHolderVertex = graph.addVertex(Collection.COLLECTION_ENTITIES_LABEL);

    existingVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, "persons");
    existingVertex.addEdge(Collection.HAS_ENTITY_NODE_RELATION_NAME,
      existingEntityHolderVertex);

    timbuctooCollection("persons", "").build(vre);

    final Vertex result = vre.getCollectionForCollectionName("persons").get().save(graphWrapper);
    assertThat(result.vertices(Direction.OUT, Collection.HAS_ENTITY_NODE_RELATION_NAME).next(),
      equalTo(existingEntityHolderVertex));
  }

  @Test
  public void saveCreatesARelationToTheArchetypeVariantCollectionVertex() {
    final Vre vre = new Vre("VreName");
    final Graph graph = newGraph()
      .withVertex(v -> {
        v.withLabel(DATABASE_LABEL);
        v.withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "persons");
        v.withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, "person");
      })
      .build();
    final GraphWrapper graphWrapper = mock(GraphWrapper.class);
    given(graphWrapper.getGraph()).willReturn(graph);

    timbuctooCollection("prefixedpersons", "prefixed").build(vre);

    final Vertex result = vre.getCollectionForCollectionName("prefixedpersons").get().save(graphWrapper);

    assertThat(result.vertices(Direction.OUT, Collection.HAS_ARCHETYPE_RELATION_NAME).hasNext(), equalTo(true));
  }

  
}
