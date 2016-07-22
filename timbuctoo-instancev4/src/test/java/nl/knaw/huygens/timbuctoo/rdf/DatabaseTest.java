package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static nl.knaw.huygens.timbuctoo.model.vre.Collection.COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.rdf.Database.RDF_URI_PROP;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DatabaseTest {

  private static final String USER_ID = "rdf-importer";
  private static final String ENTITY_RDF_URI = "http://www.example.com/entityNode";
  private static final String VRE_NAME = "vreName";
  public static final String DEFAULT_COLLECTION = VRE_NAME + "unknown";
  private static final String RELATION_NAME = "relationName";
  private Node entityNode;
  private SystemPropertyModifier modifier;


  @Before
  public void setUp() throws Exception {
    entityNode = mock(Node.class);
    when(entityNode.getURI()).thenReturn(ENTITY_RDF_URI);
    modifier = mock(SystemPropertyModifier.class);
  }

  @Test
  public void findOrCreateEntityCreateANewVertexWithTimbuctoosSystemProperties() {
    GraphWrapper graphWrapper = newGraph().withVertex(v -> v.withLabel(Vre.DATABASE_LABEL)
                                                            .withProperty("name", VRE_NAME))
                                          .withVertex(v -> {
                                            v.withLabel(Vre.DATABASE_LABEL);
                                            v.withProperty(Vre.VRE_NAME_PROPERTY_NAME, "Admin");
                                            v.withOutgoingRelation(Vre.HAS_COLLECTION_RELATION_NAME,
                                              "defaultArchetype");
                                          })
                                          .withVertex("defaultArchetype", v -> {
                                            v.withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, "concept");
                                            v.withProperty(COLLECTION_NAME_PROPERTY_NAME, "concepts");
                                            v.withOutgoingRelation(HAS_ENTITY_NODE_RELATION_NAME, "entityCollection");
                                          })
                                          .withVertex("entityCollection", v -> {
                                          })
                                          .wrap();
    final Database instance = new Database(graphWrapper, modifier);

    instance.findOrCreateEntity(VRE_NAME, entityNode);

    GraphTraversal<Vertex, Vertex> entityT = graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, ENTITY_RDF_URI);
    assertThat(entityT.hasNext(), is(true));
    Vertex entityVertex = entityT.next();
    verify(modifier).setCreated(entityVertex, USER_ID);
    verify(modifier).setModified(entityVertex, USER_ID);
    verify(modifier).setTimId(entityVertex);
    verify(modifier).setRev(entityVertex, 1);
    verify(modifier).setIsLatest(entityVertex, true);
    verify(modifier).setIsDeleted(entityVertex, false);
  }

  @Test
  public void findOrCreateEntityAddsANewlyCreatedEntityToTheDefaultCollection() {
    GraphWrapper graphWrapper = newGraph().withVertex(v -> v.withLabel(Vre.DATABASE_LABEL)
                                                            .withProperty("name", VRE_NAME))
                                          .withVertex(v -> {
                                            v.withLabel(Vre.DATABASE_LABEL);
                                            v.withProperty(Vre.VRE_NAME_PROPERTY_NAME, "Admin");
                                            v.withOutgoingRelation(Vre.HAS_COLLECTION_RELATION_NAME,
                                              "defaultArchetype");
                                          })
                                          .withVertex("defaultArchetype", v -> {
                                            v.withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, "concept");
                                            v.withProperty(COLLECTION_NAME_PROPERTY_NAME, "concepts");
                                            v.withOutgoingRelation(HAS_ENTITY_NODE_RELATION_NAME, "entityCollection");
                                          })
                                          .withVertex("entityCollection", v -> {
                                          })
                                          .wrap();
    final Database instance = new Database(graphWrapper, modifier);

    Entity entity = instance.findOrCreateEntity(VRE_NAME, entityNode);

    assertThat(entity, is(notNullValue()));
    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, ENTITY_RDF_URI)
                           .in(HAS_ENTITY_RELATION_NAME).in(HAS_ENTITY_NODE_RELATION_NAME)
                           .has(ENTITY_TYPE_NAME_PROPERTY_NAME, DEFAULT_COLLECTION).hasNext(),
      is(true));
    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, ENTITY_RDF_URI).next(),
      is(likeVertex().withLabel(DEFAULT_COLLECTION).withType(DEFAULT_COLLECTION)));
  }

  @Test
  public void findOrCreateEntityGivesABlankNodeADefaultUri() {
    GraphWrapper graphWrapper = newGraph().withVertex(v -> v.withLabel(Vre.DATABASE_LABEL)
                                                            .withProperty("name", VRE_NAME))
                                          .withVertex(v -> {
                                            v.withLabel(Vre.DATABASE_LABEL);
                                            v.withProperty(Vre.VRE_NAME_PROPERTY_NAME, "Admin");
                                            v.withOutgoingRelation(Vre.HAS_COLLECTION_RELATION_NAME,
                                              "defaultArchetype");
                                          })
                                          .withVertex("defaultArchetype", v -> {
                                            v.withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, "concept");
                                            v.withProperty(COLLECTION_NAME_PROPERTY_NAME, "concepts");
                                            v.withOutgoingRelation(HAS_ENTITY_NODE_RELATION_NAME, "entityCollection");
                                          })
                                          .withVertex("entityCollection", v -> {
                                          })
                                          .wrap();
    final Database instance = new Database(graphWrapper, modifier);
    Node blankNode = TripleHelper.createBlankNode();
    String expectedUri = VRE_NAME + ":" + blankNode.getBlankNodeLabel();

    Entity entity = instance.findOrCreateEntity(VRE_NAME, blankNode);

    assertThat(entity, is(notNullValue()));
    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP, expectedUri).hasNext(), is(true));
  }

  @Test
  public void findOrCreateCollectionReturnsTheCollectionForARequestedVre() {
    String vreName = "vreName";
    String rdfUri = "http://www.example.com/entity";
    String localName = "entity";
    GraphWrapper graphWrapper = newGraph().withVertex(v -> v.withLabel(Vre.DATABASE_LABEL)
                                                            .withProperty("name", vreName)
                                                            .withOutgoingRelation(
                                                              Vre.HAS_COLLECTION_RELATION_NAME, "collection"))
                                          .withVertex("collection", v -> v.withLabel("collection")
                                                                          .withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME,
                                                                            localName)
                                          )
                                          .withVertex(v -> {
                                            v.withLabel(Vre.DATABASE_LABEL);
                                            v.withProperty(Vre.VRE_NAME_PROPERTY_NAME, "Admin");
                                            v.withOutgoingRelation(Vre.HAS_COLLECTION_RELATION_NAME,
                                              "defaultArchetype");
                                          })
                                          .withVertex("defaultArchetype", v -> {
                                            v.withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, "concept");
                                            v.withProperty(COLLECTION_NAME_PROPERTY_NAME, "concepts");
                                            v.withIncomingRelation(HAS_ARCHETYPE_RELATION_NAME, "collection");
                                          })
                                          .wrap();
    Node collectionNode = mock(Node.class);
    when(collectionNode.getLocalName()).thenReturn(localName);
    when(collectionNode.getURI()).thenReturn(rdfUri);
    Database instance = new Database(graphWrapper);

    Collection collection = instance.findOrCreateCollection(vreName, collectionNode);

    assertThat(collection, hasProperty("vreName", equalTo(vreName)));
  }

  @Test
  public void findOrCreateCollectionReturnsTheNewlyAddedCollectionWithRdfUri() {
    String vreName = "vreName";
    String rdfUri = "http://www.example.com/entity";
    String localName = "entity";
    GraphWrapper graphWrapper = newGraph().withVertex(v -> v.withLabel(Vre.DATABASE_LABEL)
                                                            .withProperty("name", vreName))
                                          .withVertex(v -> {
                                            v.withLabel(Vre.DATABASE_LABEL);
                                            v.withProperty(Vre.VRE_NAME_PROPERTY_NAME, "Admin");
                                            v.withOutgoingRelation(Vre.HAS_COLLECTION_RELATION_NAME,
                                              "defaultArchetype");
                                          })
                                          .withVertex("defaultArchetype", v -> {
                                            v.withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, "concept");
                                            v.withProperty(COLLECTION_NAME_PROPERTY_NAME, "concepts");
                                          })
                                          .wrap();
    Node collectionNode = mock(Node.class);
    when(collectionNode.getLocalName()).thenReturn(localName);
    when(collectionNode.getURI()).thenReturn(rdfUri);
    Database instance = new Database(graphWrapper);

    Collection collection = instance.findOrCreateCollection(vreName, collectionNode);

    assertThat(collection, hasProperty("vreName", equalTo(vreName)));
    assertThat(graphWrapper.getGraph().traversal().V()
                           .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
                           .out(Vre.HAS_COLLECTION_RELATION_NAME).has(RDF_URI_PROP, rdfUri)
                           .hasNext(),
      is(true));
    assertThat(graphWrapper.getGraph().traversal().V()
                           .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
                           .out(Vre.HAS_COLLECTION_RELATION_NAME).has(RDF_URI_PROP, rdfUri)
                           .out(HAS_ENTITY_NODE_RELATION_NAME)
                           .hasNext(),
      is(true));
  }

  @Test
  public void findOrCreateCollectionAddsTheCollectionToItsArchetype() {
    String rdfUri = "http://www.example.com/entity";
    String localName = "entity";
    GraphWrapper graphWrapper = newGraph().withVertex(v -> v.withLabel(Vre.DATABASE_LABEL)
                                                            .withProperty("name", VRE_NAME))
                                          .withVertex(v -> {
                                            v.withLabel(Vre.DATABASE_LABEL);
                                            v.withProperty(Vre.VRE_NAME_PROPERTY_NAME, "Admin");
                                            v.withOutgoingRelation(Vre.HAS_COLLECTION_RELATION_NAME,
                                              "defaultArchetype");
                                          })
                                          .withVertex("defaultArchetype", v -> {
                                            v.withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, "concept");
                                            v.withProperty(COLLECTION_NAME_PROPERTY_NAME, "concepts");
                                          })
                                          .wrap();
    Node collectionNode = mock(Node.class);
    when(collectionNode.getLocalName()).thenReturn(localName);
    when(collectionNode.getURI()).thenReturn(rdfUri);
    Database instance = new Database(graphWrapper);

    Collection collection = instance.findOrCreateCollection(VRE_NAME, collectionNode);

    assertThat(collection, hasProperty("vreName", equalTo(VRE_NAME)));
    assertThat(graphWrapper.getGraph().traversal().V()
                           .hasLabel(DATABASE_LABEL).has(RDF_URI_PROP, rdfUri)
                           .out(HAS_ARCHETYPE_RELATION_NAME)
                           .hasNext(),
      is(true));
  }

  @Test
  public void findOrCreateRelationTypeCreatesANewRelationType() {
    final GraphWrapper graphWrapper = newGraph().wrap();
    final Database instance = new Database(graphWrapper);
    final String relationtypePrefix = "relationtype_";
    final Node mockNode = mock(Node.class);
    String rdfUriVal = "rdfUriVal";
    when(mockNode.getURI()).thenReturn(rdfUriVal);
    when(mockNode.getLocalName()).thenReturn(RELATION_NAME);

    final RelationType relationType = instance.findOrCreateRelationType(mockNode);

    assertThat(graphWrapper
      .getGraph().traversal().V().next(), likeVertex()
      .withLabel("relationtype")
      .withProperty("rdfUri", rdfUriVal)
      .withProperty("types", "[\"relationtype\"]")
      .withProperty(relationtypePrefix + "targetTypeName", "concept")
      .withProperty(relationtypePrefix + "sourceTypeName", "concept")
      .withProperty(relationtypePrefix + "symmetric", false)
      .withProperty(relationtypePrefix + "reflexive", false)
      .withProperty(relationtypePrefix + "derived", false)
      .withProperty(relationtypePrefix + "regularName", RELATION_NAME)
      .withProperty(relationtypePrefix + "inverseName", "inverse:" + RELATION_NAME)
      .withProperty("rev", 1)
      .withProperty("isLatest", true)
      .withProperty("created")
      .withProperty("modified")
      .withProperty("tim_id")
    );
    assertThat(relationType, allOf(
      hasProperty("rdfUri", is(rdfUriVal)),
      hasProperty("regularName", is(RELATION_NAME))
    ));
  }

  @Test
  public void findOrCreateRelationTypeReturnsAnExistingRelationType() {
    final String relationtypePrefix = "relationtype_";
    String rdfUriVal = "rdfUriVal";

    final GraphWrapper graphWrapper = newGraph().withVertex(v -> v
      .withLabel("relationtype")
      .withProperty("rdfUri", rdfUriVal)
      .withProperty("types", "[\"relationtype\"]")
      .withProperty(relationtypePrefix + "targetTypeName", "concept")
      .withProperty(relationtypePrefix + "sourceTypeName", "concept")
      .withProperty(relationtypePrefix + "symmetric", false)
      .withProperty(relationtypePrefix + "reflexive", false)
      .withProperty(relationtypePrefix + "derived", false)
      .withProperty(relationtypePrefix + "regularName", RELATION_NAME)
      .withProperty(relationtypePrefix + "inverseName", "inverse:" + RELATION_NAME)
      .withProperty("rev", 1)
      .withProperty("isLatest", true)
    ).wrap();
    final Database instance = new Database(graphWrapper);
    final Node mockNode = mock(Node.class);
    when(mockNode.getURI()).thenReturn(rdfUriVal);
    when(mockNode.getLocalName()).thenReturn(RELATION_NAME);

    instance.findOrCreateRelationType(mockNode);

    assertThat(graphWrapper.getGraph().traversal().V().hasLabel("relationtype").count().next(), is(1L));
  }

  @Test
  public void isKnowArchetypeChecksIfTheCollectionWithTheNameIsAKnowArchetype() {
    GraphWrapper graphWrapper = newGraph().withVertex(v -> v.withLabel(Vre.DATABASE_LABEL)
                                                            .withProperty(Vre.VRE_NAME_PROPERTY_NAME, "Admin")
                                                            .withOutgoingRelation(Vre.HAS_COLLECTION_RELATION_NAME,
                                                              "defaultArchetype")
                                                            .withOutgoingRelation(Vre.HAS_COLLECTION_RELATION_NAME,
                                                              "knownArchetype"))

                                          .withVertex("defaultArchetype", v ->
                                            v.withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, "concept")
                                             .withProperty(COLLECTION_NAME_PROPERTY_NAME, "concepts"))
                                          .withVertex("knownArchetype", v ->
                                            v.withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, "knownArchetype")
                                             .withProperty(COLLECTION_NAME_PROPERTY_NAME, "knownArchetypes"))
                                          .wrap();
    Database instance = new Database(graphWrapper);

    assertThat(instance.isKnownArchetype("knownArchetype"), is(true));
    assertThat(instance.isKnownArchetype("unknownArchetype"), is(false));
  }

  @Test
  public void findEntitiesByCollectionReturnsAllTheEntitiesOfTheCollection() {
    CollectionDescription desc = CollectionDescription.createCollectionDescription("collection", VRE_NAME);
    GraphWrapper graphWrapper = newGraph()
      .withVertex("collection", v -> v.withLabel(DATABASE_LABEL)
                                      .withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, desc.getEntityTypeName())
                                      .withProperty(COLLECTION_NAME_PROPERTY_NAME, desc.getCollectionName())
                                      .withOutgoingRelation(HAS_ENTITY_NODE_RELATION_NAME, "entityVertex"))
      .withVertex("entityVertex", v -> v.withOutgoingRelation(HAS_ENTITY_RELATION_NAME, "entity1")
                                        .withOutgoingRelation(HAS_ENTITY_RELATION_NAME, "entity2"))
      .withVertex("entity1", v -> {
      })
      .withVertex("entity2", v -> {
      })
      .withVertex("entityOfOtherCollection", v -> {
      })
      .wrap();
    Database instance = new Database(graphWrapper);
    Collection collection = mock(Collection.class);
    when(collection.getDescription())
      .thenReturn(desc);

    Set<Entity> entitiesByCollection = instance.findEntitiesByCollection(collection);

    assertThat(entitiesByCollection, hasSize(2));
  }
}
