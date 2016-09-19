package nl.knaw.huygens.timbuctoo.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.util.TestGraphBuilder;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.HAS_COLLECTION_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.KEYWORD_TYPES_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.VRE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.VreBuilder.vre;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

public class LoadSaveVreTest {

  private Graph initGraph() {
    return initGraph(c -> {
    });
  }

  private Graph initGraph(Consumer<TestGraphBuilder> init) {
    TestGraphBuilder testGraphBuilder = newGraph();
    init.accept(testGraphBuilder);
    return testGraphBuilder.build();
  }

  private Vertex save(Vre vre, Graph graph) {
    return vre.save(graph);
  }

  private Vre load(Vertex vertex) {
    return Vre.load(vertex);
  }

  private Vertex makeVreVertex(Consumer<TestGraphBuilder> testGraphBuilderConsumer) {
    return initGraph(testGraphBuilderConsumer)
      .traversal().V()
      .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
      .next();
  }

  @Test
  public void saveCreatesAVertexForTheVre() {
    final Vre vre = new Vre("VreName");

    final Vertex result = save(vre, initGraph());

    assertThat(result, likeVertex()
      .withLabel(Vre.DATABASE_LABEL)
      .withProperty(VRE_NAME_PROPERTY_NAME, "VreName")
    );
  }

  @Test
  public void saveReplacesAnExistingVertexForTheVre() {
    Graph graph = initGraph(b -> b.withVertex(
      v -> v
        .withLabel(Vre.DATABASE_LABEL)
        .withProperty(VRE_NAME_PROPERTY_NAME, "VreName")
    ));
    final Vre vre = new Vre("VreName");

    final Vertex result = save(vre, graph);

    assertThat(result.label(), is(Vre.DATABASE_LABEL));
    assertThat(result.value(VRE_NAME_PROPERTY_NAME), is("VreName"));
    assertThat(graph.traversal().V().count().next(), is(1L));
  }

  @Test
  public void saveAddsKeywordTypesWhenAvailable() throws JsonProcessingException {
    Map<String, String> keyWordTypes = Maps.newHashMap();
    keyWordTypes.put("typeA", "valueA");
    keyWordTypes.put("typeB", "valueB");
    final Vre vre = new Vre("VreName", keyWordTypes);

    final Vertex result = save(vre, initGraph());

    assertThat(result.property(KEYWORD_TYPES_PROPERTY_NAME).value(),
      equalTo(new ObjectMapper().writeValueAsString(keyWordTypes))
    );
  }

  @Test
  public void saveAddsVreCollectionsToTheVre() {
    Vre vre = vre("VreName", "prefix")
      .withCollection("prefixpersons")
      .withCollection("prefixdocuments")
      .build();

    final Vertex savedVertex = save(vre, initGraph());
    final List<Vertex> result = Lists.newArrayList(savedVertex.vertices(Direction.OUT, HAS_COLLECTION_RELATION_NAME));

    assertThat(result, containsInAnyOrder(
      likeVertex()
        .withLabel(Collection.DATABASE_LABEL)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "prefixpersons"),
      likeVertex()
        .withLabel(Collection.DATABASE_LABEL)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, "prefixdocuments")
    ));
  }

  @Test
  public void loadLoadsAVreFromAVertex() throws JsonProcessingException {
    final Map<String, String> keyWordTypes = new HashMap<>();
    keyWordTypes.put("keyword", "type");
    String stringifiedKeyWordTypes = new ObjectMapper().writeValueAsString(keyWordTypes);

    Vertex source = makeVreVertex(g ->
      g.withVertex(v ->
        v.withLabel(Vre.DATABASE_LABEL)
          .withProperty(VRE_NAME_PROPERTY_NAME, "VreName")
          .withProperty(KEYWORD_TYPES_PROPERTY_NAME, stringifiedKeyWordTypes)
      ));
    final Vre instance = load(source);

    assertThat(instance.getVreName(), equalTo("VreName"));
    assertThat(instance.getKeywordTypes().get("keyword"), equalTo("type"));
  }

  @Test
  public void loadLoadsTheCollections() throws JsonProcessingException {
    final String entityTypeName = "person";
    final String collectionName = "persons";
    final HashMap<String, String> keywordTypes = new HashMap<>();
    keywordTypes.put("keyword", "type");
    String stringifiedKeyWordTypes = new ObjectMapper().writeValueAsString(keywordTypes);

    Vertex source = makeVreVertex(g -> g
      .withVertex("vre", v -> v
        .withLabel(Vre.DATABASE_LABEL)
        .withProperty(VRE_NAME_PROPERTY_NAME, "VreName")
        .withProperty(KEYWORD_TYPES_PROPERTY_NAME, stringifiedKeyWordTypes)
      )
      .withVertex(v -> v
        .withLabel(entityTypeName)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, collectionName)
        .withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName)
        .withProperty(Collection.IS_RELATION_COLLECTION_PROPERTY_NAME, false)
        .withIncomingRelation(HAS_COLLECTION_RELATION_NAME, "vre")
      ));


    final Vre instance = load(source);
    final Collection collectionByName = instance.getCollectionForCollectionName(collectionName).get();

    assertThat(instance.getKeywordTypes().get("keyword"), equalTo("type"));
    assertThat(collectionByName, instanceOf(Collection.class));
    assertThat(instance.getCollectionForTypeName(entityTypeName), instanceOf(Collection.class));
    assertThat(instance.getCollections().get(entityTypeName), instanceOf(Collection.class));
    assertThat(instance.getEntityTypes(), contains(
      entityTypeName
    ));
    assertThat(instance.getImplementerOf("person").get(), equalTo(collectionByName));
    assertThat(instance.getOwnType("notmytype", "person"), equalTo(entityTypeName));
  }

  @Test
  public void loadLoadsInheritingCollections() {
    final String entityTypeName = "wwperson";
    final String collectionName = "wwpersons";
    final String archetypeName = "person";
    final String archetypeCollectionName = "persons";

    Vertex vertex = makeVreVertex(g -> g
      .withVertex("vre", v -> v
        .withLabel(Vre.DATABASE_LABEL)
        .withProperty(VRE_NAME_PROPERTY_NAME, "VreName")
      )
      .withVertex("collection", v -> v
        .withLabel(Collection.DATABASE_LABEL)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, collectionName)
        .withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName)
        .withProperty(Collection.IS_RELATION_COLLECTION_PROPERTY_NAME, false)
        .withIncomingRelation(HAS_COLLECTION_RELATION_NAME, "vre")
      )
      .withVertex("archetype", v -> v
        .withLabel(Collection.DATABASE_LABEL)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, archetypeCollectionName)
        .withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, archetypeName)
        .withIncomingRelation(HAS_ARCHETYPE_RELATION_NAME, "collection")
      ));

    final Vre instance = load(vertex);

    assertThat(instance.getImplementerOf("person").get().getEntityTypeName(), equalTo(entityTypeName));
  }

  @Test
  public void loadLoadsRelationCollections() {
    final String entityTypeName = "relation";
    final String collectionName = "relations";

    Vertex vertex = makeVreVertex(g -> g
      .withVertex("vre", v -> v
        .withLabel(Vre.DATABASE_LABEL)
        .withProperty(VRE_NAME_PROPERTY_NAME, "VreName")
      )
      .withVertex("collection", v -> v
        .withLabel(entityTypeName)
        .withProperty(Collection.COLLECTION_NAME_PROPERTY_NAME, collectionName)
        .withProperty(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName)
        .withProperty(Collection.IS_RELATION_COLLECTION_PROPERTY_NAME, true)
        .withIncomingRelation(HAS_COLLECTION_RELATION_NAME, "vre")
      ));


    final Vre instance = load(vertex);
    final Collection collectionByName = instance.getCollectionForCollectionName(collectionName).get();

    assertThat(instance.getRelationCollection().get(), equalTo(collectionByName));
  }

}
