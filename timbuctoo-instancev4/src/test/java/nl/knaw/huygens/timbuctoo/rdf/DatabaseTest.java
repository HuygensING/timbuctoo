package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.model.vre.Collection.COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.rdf.CollectionDescription.DEFAULT_COLLECTION_NAME;
import static nl.knaw.huygens.timbuctoo.rdf.Database.RDF_URI_PROP;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DatabaseTest {

  public static final String USER_ID = "rdf-importer";
  public static final String TEST_URI = "http://www.example.com/node";
  public static final String VRE_NAME = "vreName";
  private GraphWrapper graphWrapper;
  private Node node;
  private SystemPropertyModifier modifier;


  @Before
  public void setUp() throws Exception {
    graphWrapper = newGraph().wrap();
    node = mock(Node.class);
    when(node.getURI()).thenReturn(TEST_URI);
    modifier = mock(SystemPropertyModifier.class);
  }

  @Test
  public void findOrCreateEntityVertexCreateANewVertexWithTimbuctoosSystemProperties() {
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
    final CollectionDescription collectionDescription = CollectionDescription.getDefault(VRE_NAME);
    final CollectionMapper collectionMapper = new CollectionMapper(graphWrapper);
    final Database instance = new Database(graphWrapper, modifier, collectionMapper);

    Vertex vertex = instance.findOrCreateEntityVertex(node, collectionDescription);

    assertThat(graphWrapper.getGraph().traversal().V().has(RDF_URI_PROP).next(), is(vertex));
    verify(modifier).setCreated(vertex, USER_ID);
    verify(modifier).setModified(vertex, USER_ID);
    verify(modifier).setTimId(vertex);
    verify(modifier).setRev(vertex, 1);
    verify(modifier).setIsLatest(vertex, true);
    verify(modifier).setIsDeleted(vertex, false);
  }

  @Test
  public void findOrCreateEntityVertexAddsANewlyCreatedEntityToTheDefaultCollection() {
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
    final CollectionMapper collectionMapper = new CollectionMapper(graphWrapper);
    final Database instance = new Database(graphWrapper, modifier, collectionMapper);
    final CollectionDescription collectionDescription = CollectionDescription.getDefault(VRE_NAME);

    Vertex vertex = instance.findOrCreateEntityVertex(node, collectionDescription);

    assertThat(graphWrapper.getGraph().traversal().V(vertex.id())
                           .in(HAS_ENTITY_RELATION_NAME).in(HAS_ENTITY_NODE_RELATION_NAME)
                           .has(ENTITY_TYPE_NAME_PROPERTY_NAME, DEFAULT_COLLECTION_NAME).hasNext(),
      is(true));
  }

  @Test // FIXME use rdf uri again
  public void findOrCreateCollectionReturnsTheCollectionWithARdfUriForARequestedVre() {
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

  @Test // FIXME use rdf uri again
  public void findOrCreateCollectionReturnsTheNewlyAddedCollectionWhenItDidNotExist() {
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
                           .out(Vre.HAS_COLLECTION_RELATION_NAME).has(ENTITY_TYPE_NAME_PROPERTY_NAME, localName)
                           .hasNext(),
      is(true));
  }

  @Test // FIXME use rdf uri again
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
                           .hasLabel(DATABASE_LABEL).has(ENTITY_TYPE_NAME_PROPERTY_NAME, localName)
                           .out(HAS_ARCHETYPE_RELATION_NAME)
                           .hasNext(),
      is(true));
  }
}
