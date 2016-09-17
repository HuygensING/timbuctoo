package nl.knaw.huygens.timbuctoo.model.vre;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.vre.Vre.DATABASE_LABEL;
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

public class VreTest {
  private Graph graph;

  @Before
  public void setUp() {
    graph = newGraph().build();
  }

  private Vertex save(Vre vre) {
    return vre.save(graph);
  }

  private Vre load(Vertex vertex) {
    return Vre.load(vertex);
  }

  @Test
  public void saveCreatesAVertexForTheVre() {
    final Vre vre = new Vre("VreName");

    final Vertex result = save(vre);

    assertThat(result, likeVertex()
      .withLabel(DATABASE_LABEL)
      .withProperty(VRE_NAME_PROPERTY_NAME, "VreName")
    );
  }

  @Test
  public void saveReplacesAnExistingVertexForTheVre() {
    final Vertex existingVertex = graph.addVertex(DATABASE_LABEL);
    existingVertex.property(VRE_NAME_PROPERTY_NAME, "VreName");
    final Vre vre = new Vre("VreName");

    final Vertex result = save(vre);

    assertThat(result, equalTo(existingVertex));
  }

  @Test
  public void saveAddsKeywordTypesWhenAvailable() throws JsonProcessingException {
    Map<String, String> keyWordTypes = Maps.newHashMap();
    keyWordTypes.put("typeA", "valueA");
    keyWordTypes.put("typeB", "valueB");
    final Vre vre = new Vre("VreName", keyWordTypes);

    final Vertex result = save(vre);

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

    final Vertex savedVertex = save(vre);
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
    final Vertex vertex = graph.addVertex("VRE");
    final Map<String, String> keyWordTypes = new HashMap<>();

    keyWordTypes.put("keyword", "type");
    vertex.property(VRE_NAME_PROPERTY_NAME, "VreName");
    vertex.property(KEYWORD_TYPES_PROPERTY_NAME, new ObjectMapper().writeValueAsString(keyWordTypes));
    final Vre instance = load(vertex);

    assertThat(instance.getVreName(), equalTo("VreName"));
    assertThat(instance.getKeywordTypes().get("keyword"), equalTo("type"));
  }

  @Test
  public void loadLoadsTheCollections() throws JsonProcessingException {
    final String entityTypeName = "person";
    final String collectionName = "persons";
    final HashMap<String, String> keywordTypes = new HashMap<>();
    keywordTypes.put("keyword", "type");
    final Vertex vertex = graph.addVertex("VRE");
    final Vertex collectionVertex = graph.addVertex(entityTypeName);
    vertex.property(VRE_NAME_PROPERTY_NAME, "VreName");
    vertex.property(KEYWORD_TYPES_PROPERTY_NAME, new ObjectMapper().writeValueAsString(keywordTypes));
    collectionVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, collectionName);
    collectionVertex.property(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName);
    collectionVertex.property(Collection.IS_RELATION_COLLECTION_PROPERTY_NAME, false);
    vertex.addEdge(HAS_COLLECTION_RELATION_NAME, collectionVertex);

    final Vre instance = load(vertex);
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
    final Vertex vertex = graph.addVertex("VRE");
    final String entityTypeName = "wwperson";
    final String collectionName = "wwpersons";
    final String archetypeName = "person";
    final String archetypeCollectionName = "persons";
    final Vertex archetypeVertex = graph.addVertex("collection");
    final Vertex collectionVertex = graph.addVertex("collection");
    vertex.property(VRE_NAME_PROPERTY_NAME, "VreName");
    collectionVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, collectionName);
    collectionVertex.property(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName);
    collectionVertex.property(Collection.IS_RELATION_COLLECTION_PROPERTY_NAME, false);
    archetypeVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, archetypeCollectionName);
    archetypeVertex.property(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, archetypeName);
    vertex.addEdge(HAS_COLLECTION_RELATION_NAME, collectionVertex);
    collectionVertex.addEdge(Collection.HAS_ARCHETYPE_RELATION_NAME, archetypeVertex);

    final Vre instance = load(vertex);

    assertThat(instance.getImplementerOf("person").get().getEntityTypeName(), equalTo(entityTypeName));
  }

  @Test
  public void loadLoadsRelationCollections() {
    final Vertex vertex = graph.addVertex("VRE");
    final String entityTypeName = "relation";
    final String collectionName = "relations";
    final Vertex collectionVertex = graph.addVertex(entityTypeName);
    vertex.property(VRE_NAME_PROPERTY_NAME, "VreName");
    collectionVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, collectionName);
    collectionVertex.property(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName);
    collectionVertex.property(Collection.IS_RELATION_COLLECTION_PROPERTY_NAME, true);
    vertex.addEdge(HAS_COLLECTION_RELATION_NAME, collectionVertex);

    final Vre instance = load(vertex);
    final Collection collectionByName = instance.getCollectionForCollectionName(collectionName).get();

    assertThat(instance.getRelationCollection().get(), equalTo(collectionByName));
  }
}
