package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.rdf.CollectionDescription;
import nl.knaw.huygens.timbuctoo.rdf.CollectionMapper;
import nl.knaw.huygens.timbuctoo.rdf.PropertyHelper;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CollectionMapperTest {

  public static final String VRE_NAME = "vreName";
  private CollectionMapper instance;
  private Graph graph;
  private PropertyHelper propertyHelper;

  @Before
  public void setUp() throws Exception {
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> {
        v.withLabel(Vre.DATABASE_LABEL);
        v.withProperty(Vre.VRE_NAME_PROPERTY_NAME, VRE_NAME);
      })
      .withVertex(v -> {
        v.withLabel(Vre.DATABASE_LABEL);
        v.withProperty(Vre.VRE_NAME_PROPERTY_NAME, "Admin");
        v.withOutgoingRelation(Vre.HAS_COLLECTION_RELATION_NAME, "defaultArchetype");
      })
      .withVertex("defaultArchetype", v -> {
        v.withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "concept");
        v.withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "concepts");
      })
      .withVertex("archetypeHolder", v -> {
        v.withIncomingRelation(Collection.HAS_ENTITY_NODE_RELATION_NAME, "defaultArchetype");
      })
      .wrap();
    propertyHelper = mock(PropertyHelper.class);
    instance = new CollectionMapper(graphWrapper, propertyHelper);
    graph = graphWrapper.getGraph();
  }

  @Test
  public void addToCollectionCreatesACollectionVertexWithAnEntityTypeNameAndCollectionName() {
    instance.addToCollection(graph.addVertex(), new CollectionDescription("test", VRE_NAME));

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL).next(),
      is(likeVertex().withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "test")
                     .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "tests")));
  }


  @Test
  public void addToCollectionAddsOneEntityNodeToTheCollection() {
    instance.addToCollection(graph.addVertex(), new CollectionDescription("test", VRE_NAME));

    assertThat(graph.traversal().V()
                    .hasLabel(Collection.DATABASE_LABEL).out(Collection.HAS_ENTITY_NODE_RELATION_NAME).count().next(),
      is(1L));
  }

  @Test
  public void addToCollectionAddsTheCollectionToTheRequestedVre() {
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
    instance.addToCollection(graph.addVertex(), new CollectionDescription("test", VRE_NAME));
    instance.addToCollection(graph.addVertex(), new CollectionDescription("test", VRE_NAME));

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL).out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(2L));
  }

  @Test
  public void addToCollectionDoesNotConnectAVertexMoreThanOnce() {
    Vertex vertex = graph.addVertex();
    instance.addToCollection(vertex, new CollectionDescription("test", VRE_NAME));
    instance.addToCollection(vertex, new CollectionDescription("test", VRE_NAME));

    assertThat(graph.traversal().V().hasLabel(Collection.DATABASE_LABEL).out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .out(Collection.HAS_ENTITY_RELATION_NAME).count().next(),
      is(1L));
  }

  @Test
  public void addToCollectionChangesTheCollectionIfThePreviousCollectionWasUnknown() {
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
  public void addToCollectionSetsTheTypesArray() {
    Vertex vertex = graph.addVertex();

    instance.addToCollection(vertex, new CollectionDescription("test", VRE_NAME));
    instance.addToCollection(vertex, new CollectionDescription("other", VRE_NAME));

    assertThat(getEntityTypesOrDefault(vertex), arrayContainingInAnyOrder(
      "test", "other", "concept"
    ));
  }

  @Test
  public void addToCollectionSetsTheEntityTypeLabels() {
    Vertex vertex = graph.addVertex();

    instance.addToCollection(vertex, new CollectionDescription("test", VRE_NAME));
    instance.addToCollection(vertex, new CollectionDescription("other", VRE_NAME));

    assertThat(graph.traversal().V(vertex.id())
                           .where(
                             __.has(T.label, LabelP.of("test"))
                               .has(T.label, LabelP.of("other"))
                               .has(T.label, LabelP.of("concept"))
                           ).hasNext(),
      is(true)
    );
  }

  @Test
  public void addToCollectionAddsTheCollectionToTheConceptsArchetype() {
    Vertex vertex = graph.addVertex();

    instance.addToCollection(vertex, CollectionDescription.getDefault(VRE_NAME));

    assertThat(graph.traversal().V(vertex.id())
          .in(Collection.HAS_ENTITY_RELATION_NAME)
          .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
          .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "unknown")
          .out(Collection.HAS_ARCHETYPE_RELATION_NAME)
          .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "concept")
          .hasNext(),
      is(true));
  }

  @Test
  public void addToCollectionAddsTheCollectionToTheConceptsArchetypeOnlyOnce() {
    Vertex vertex = graph.addVertex();
    final CollectionDescription collectionDescription = new CollectionDescription("test", VRE_NAME);

    instance.addToCollection(vertex, collectionDescription);
    instance.addToCollection(vertex, collectionDescription);
    instance.addToCollection(vertex, collectionDescription);

    assertThat(graph.traversal().V(vertex.id())
                    .in(Collection.HAS_ENTITY_RELATION_NAME)
                    .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "test")
                    .out(Collection.HAS_ARCHETYPE_RELATION_NAME)
                    .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "concept")
                    .count().next(),
      is(1L));
  }

  @Test
  public void addToCollectionAddsTheEntityVertexToTheConceptsArchetype() {
    Vertex vertex = graph.addVertex();

    instance.addToCollection(vertex, CollectionDescription.getDefault(VRE_NAME));

    assertThat(graph.traversal().V(vertex.id())
          .in(Collection.HAS_ENTITY_RELATION_NAME)
          .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
          .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "concept")
          .hasNext(),
      is(true)
    );
  }

  @Test
  public void addToCollectionAddsTheEntityVertexToTheConceptsArchetypeOnlyOnce() {
    Vertex vertex = graph.addVertex();

    instance.addToCollection(vertex, new CollectionDescription("test", VRE_NAME));
    instance.addToCollection(vertex, new CollectionDescription("unknown", VRE_NAME));
    instance.addToCollection(vertex, new CollectionDescription("other", VRE_NAME));

    assertThat(graph.traversal().V(vertex.id())
                    .in(Collection.HAS_ENTITY_RELATION_NAME)
                    .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
                    .has(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "concept")
                    .count().next(),
      is(1L)
    );
  }


  @Test
  public void addToCollectionDuplicatesThePropertiesForANewCollectionType() {
    Vertex vertex = graph.addVertex();

    CollectionDescription collectionDescription = new CollectionDescription("test", VRE_NAME);
    instance.addToCollection(vertex, collectionDescription);

    verify(propertyHelper).setCollectionProperties(argThat(is(vertex)), argThat(is(collectionDescription)), any());
  }

  @Test
  public void getCollectionDescriptionsReturnsTheCollectionDescriptionsOfTheVertex() {
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

  @Test
  public void getCollectionDescriptionsDoesNotReturnTheArchetypeOfTheVertex() {
    Vertex vertex = graph.addVertex();
    instance.addToCollection(vertex, new CollectionDescription("test", VRE_NAME));
    instance.addToCollection(vertex, new CollectionDescription("unknown", VRE_NAME));
    instance.addToCollection(vertex, new CollectionDescription("other", VRE_NAME));

    final List<String> result = instance
      .getCollectionDescriptions(vertex, VRE_NAME).stream()
      .map(CollectionDescription::getEntityTypeName)
      .filter(entityTypeName -> entityTypeName.equals("concept"))
      .collect(Collectors.toList());

    assertThat(result.size(), is(0));
  }

}
