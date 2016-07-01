package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class HuygensIngConfigToDatabaseMigrationIntegrationTest {
  private GraphWrapper graphWrapper;

  @Before
  public void setUp() {
    Graph graph = newGraph()
      // set up 2 documents
      .withVertex(v -> v.withLabel("document").withProperty("prop", "AdminDocument1"))
      .withVertex(v -> v.withLabel("document").withProperty("prop", "AdminDocument2"))
      // set up 3 persons
      .withVertex(v -> v.withLabel("person").withProperty("prop", "AdminPerson1"))
      .withVertex(v -> v.withLabel("person").withProperty("prop", "AdminPerson2"))
      .withVertex(v -> v.withLabel("person").withProperty("prop", "AdminPerson3"))
      // set up 1 prefixbperson
      .withVertex(v -> v.withLabel("prefixbperson").withProperty("prop", "VreBPerson1"))
      // set up 2 prefixadocuments
      .withVertex(v -> v.withLabel("prefixadocument").withProperty("prop", "VreADocument1"))
      .withVertex(v -> v.withLabel("prefixadocument").withProperty("prop", "VreADocument2"))
      // set up NO prefixapersons
      .build();

    this.graphWrapper = mock(GraphWrapper.class);
    given(graphWrapper.getGraph()).willReturn(graph);


    // Mock the getCurrentEntitiesFor method to return all the vertices labeled entityName
    // (just like the real thing, but without isLatest restriction).
    final String[] entityTypeNames = {"document", "person", "prefixaperson", "prefixbperson", "prefixadocument"};
    for (String entityTypeName : entityTypeNames) {
      given(graphWrapper.getCurrentEntitiesFor(entityTypeName))
        .willAnswer(new Answer<GraphTraversal<Vertex, Vertex>>() {
          @Override
          public GraphTraversal<Vertex, Vertex> answer(InvocationOnMock invocationOnMock) throws Throwable {
            return graph.traversal().V().hasLabel(entityTypeName);
          }
        });
    }
  }

  @Test
  public void executeSavesTheVreMappingsToAGraph() throws IOException {
    final Vres mappings = new Vres.Builder()
      .withVre("Admin", "", vre -> {
        vre
          .withCollection("persons")
          .withCollection("documents");
      })
      .withVre("VreA", "prefixa", vre -> {
        vre
          .withCollection("prefixapersons")
          .withCollection("prefixadocuments");
      })
      .withVre("VreB", "prefixb", vre -> {
        vre
          .withCollection("prefixbpersons");
      })
      .build();
    final Map<String, Map<String, String>> keywordTypeMappings = new HashMap<>();
    final Map<String, String> keywordTypes = new HashMap<>();
    keywordTypes.put("keywordA", "valueA");
    keywordTypeMappings.put("VreA", keywordTypes);

    new HuygensIngConfigToDatabaseMigration(mappings, keywordTypeMappings).execute(graphWrapper);

    final List<Vertex> vreResult = graphWrapper.getGraph().traversal().V().hasLabel(Vre.DATABASE_LABEL).toList();
    final List<Vertex> collectionsForAdmin = getCollectionsForVre("Admin");
    final List<Vertex> collectionsForVreA = getCollectionsForVre("VreA");
    final List<Vertex> collectionsForVreB = getCollectionsForVre("VreB");
    final List<Vertex> collectionsFromPersonArchetype = getCollectionsForArchetype("persons");
    final List<Vertex> collectionsFromDocumentArchetype = getCollectionsForArchetype("documents");


    // From the configuration 3 resulting VRE vertices are expected
    assertThat(vreResult, containsInAnyOrder(
      likeVertex()
        .withLabel(Vre.DATABASE_LABEL)
        .withProperty(Vre.VRE_NAME_PROPERTY_NAME, "Admin")
        .withoutProperty(Vre.KEYWORD_TYPES_PROPERTY_NAME),

      likeVertex()
        .withLabel(Vre.DATABASE_LABEL)
        .withProperty(Vre.VRE_NAME_PROPERTY_NAME, "VreA")
        .withProperty(Vre.KEYWORD_TYPES_PROPERTY_NAME, new ObjectMapper().writeValueAsString(keywordTypes)),

      likeVertex()
        .withLabel(Vre.DATABASE_LABEL)
        .withProperty(Vre.VRE_NAME_PROPERTY_NAME, "VreB")
        .withoutProperty(Vre.KEYWORD_TYPES_PROPERTY_NAME)
    ));

    // Two collections in the Admin VRE
    assertThat(collectionsForAdmin, containsInAnyOrder(
      likeVertex()
        .withLabel(Collection.DATABASE_LABEL)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "persons"),

      likeVertex()
        .withLabel(Collection.DATABASE_LABEL)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "documents")
    ));

    // Two collections in VreA
    assertThat(collectionsForVreA, containsInAnyOrder(
      likeVertex()
        .withLabel(Collection.DATABASE_LABEL)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "prefixapersons"),

      likeVertex()
        .withLabel(Collection.DATABASE_LABEL)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "prefixadocuments")
    ));

    // One collection in VreB
    assertThat(collectionsForVreB, contains(
      likeVertex()
        .withLabel(Collection.DATABASE_LABEL)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "prefixbpersons")
    ));

    // Two collections that have the "persons" collection as archetype
    assertThat(collectionsFromPersonArchetype, containsInAnyOrder(
      likeVertex().withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "prefixapersons"),
      likeVertex().withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "prefixbpersons")
    ));

    // One collection that has the "documents" collection as archetype
    assertThat(collectionsFromDocumentArchetype, contains(
      likeVertex().withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "prefixadocuments")
    ));
  }


  @Test
  public void executeAddsTypesLabeledEntitiesToTheCollectionHolders() throws IOException {
    final Vres mappings = new Vres.Builder()
      .withVre("Admin", "", vre -> {
        vre
          .withCollection("persons")
          .withCollection("documents");
      })
      .withVre("VreA", "prefixa", vre -> {
        vre
          .withCollection("prefixapersons")
          .withCollection("prefixadocuments");
      })
      .withVre("VreB", "prefixb", vre -> {
        vre
          .withCollection("prefixbpersons");
      })
      .build();

    new HuygensIngConfigToDatabaseMigration(mappings, Maps.newHashMap()).execute(graphWrapper);

    final List<Vertex> persons = getEntitiesForCollection("persons");
    final List<Vertex> documents = getEntitiesForCollection("documents");
    final List<Vertex> prefixapersons = getEntitiesForCollection("prefixapersons");
    final List<Vertex> prefixbpersons = getEntitiesForCollection("prefixbpersons");
    final List<Vertex> prefixadocuments = getEntitiesForCollection("prefixadocuments");

    assertThat(persons, containsInAnyOrder(
      likeVertex().withLabel("person").withProperty("prop", "AdminPerson1"),
      likeVertex().withLabel("person").withProperty("prop", "AdminPerson2"),
      likeVertex().withLabel("person").withProperty("prop", "AdminPerson3")
    ));

    assertThat(documents, containsInAnyOrder(
      likeVertex().withLabel("document").withProperty("prop", "AdminDocument1"),
      likeVertex().withLabel("document").withProperty("prop", "AdminDocument2")
    ));

    assertThat(prefixbpersons, contains(
      likeVertex().withLabel("prefixbperson").withProperty("prop", "VreBPerson1")
    ));

    assertThat(prefixadocuments, containsInAnyOrder(
      likeVertex().withLabel("prefixadocument").withProperty("prop", "VreADocument1"),
      likeVertex().withLabel("prefixadocument").withProperty("prop", "VreADocument2")
    ));

    assertThat(prefixapersons.size(), equalTo(0));
  }

  /**
   * @param collectionName the name of the collection
   * @return list of all entities belonging to collection "collectionName"
   */
  private List<Vertex> getEntitiesForCollection(String collectionName) {
    return graphWrapper.getGraph().traversal().V().hasLabel(Collection.DATABASE_LABEL)
                       .has(Collection.COLLECTION_NAME_PROPERTY_NAME, collectionName)
                       .outE(Collection.HAS_ENTITY_NODE_RELATION_NAME).inV()
                       .outE(Collection.HAS_ENTITY_RELATION_NAME).inV()
                       .toList();
  }

  /**
   * @param archetypeCollectionName the name of the archetype collection
   * @return list of all collections that have "archetypeCollectionName" for an archetype
   */
  private List<Vertex> getCollectionsForArchetype(String archetypeCollectionName) {
    return graphWrapper.getGraph().traversal().V()
                       .hasLabel(Collection.DATABASE_LABEL)
                       .has(Collection.COLLECTION_NAME_PROPERTY_NAME, archetypeCollectionName)
                       .inE(Collection.HAS_ARCHETYPE_RELATION_NAME)
                       .outV()
                       .toList();
  }

  /**
   * @param vreName the name of the VRE
   * @return list of all collections that have "vreName" as their name property
   */
  private List<Vertex> getCollectionsForVre(String vreName) {
    return graphWrapper.getGraph().traversal().V()
                       .hasLabel(Vre.DATABASE_LABEL)
                       .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
                       .outE(Vre.HAS_COLLECTION_RELATION_NAME).inV()
                       .toList();
  }
}
