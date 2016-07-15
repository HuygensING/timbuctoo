package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.HAS_NEXT_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_NODE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ENTITY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_INITIAL_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CollectionTest {

  public static final String VRE_NAME = "vreName";
  public static final String COLLECTION_NAME = "collectionName";
  public static final String ENTITY_NAME = "entityName";

  @Test
  public void addPropertyAddsThePropertyForTheCurrentCollectionToTheEntityVertex() {
    CollectionDescription description = mock(CollectionDescription.class);
    String collectionPropertyName = "propertyName";
    when(description.createPropertyName(anyString())).thenReturn(collectionPropertyName);
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v.withLabel(DATABASE_LABEL)
                        .withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, ENTITY_NAME)
                        .withProperty(COLLECTION_NAME_PROPERTY_NAME, COLLECTION_NAME))
      .wrap();
    Vertex collectionVertex = graphWrapper.getGraph().traversal().V().next();
    Collection instance = new Collection(VRE_NAME, collectionVertex, graphWrapper, description);
    Vertex entityVertex = mock(Vertex.class);
    String propValue = "propValue";

    instance.addProperty(entityVertex, "propName", propValue);

    verify(entityVertex).property(collectionPropertyName, propValue);
  }

  @Test
  public void addPropertyAddsThePropertyConfigurationOfThePropertyToTheCollectionVertex() {
    CollectionDescription description = mock(CollectionDescription.class);
    String collectionPropertyName = "propertyName";
    when(description.createPropertyName(anyString())).thenReturn(collectionPropertyName);
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v.withLabel(DATABASE_LABEL)
                        .withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, ENTITY_NAME)
                        .withProperty(COLLECTION_NAME_PROPERTY_NAME, COLLECTION_NAME))
      .wrap();
    Vertex collectionVertex = graphWrapper.getGraph().traversal().V().next();
    Collection instance = new Collection(VRE_NAME, collectionVertex, graphWrapper, description);
    Vertex entityVertex = mock(Vertex.class);
    String propValue = "propValue";

    String propName = "propName";
    instance.addProperty(entityVertex, propName, propValue);

    assertThat(graphWrapper.getGraph().traversal().V(collectionVertex.id())
                           .out(HAS_PROPERTY_RELATION_NAME)
                           .has("dbName", collectionPropertyName).has("clientName", propName)
                           .count().next(),
      is(1L));
    assertThat(graphWrapper.getGraph().traversal().V(collectionVertex.id())
                           .out(HAS_INITIAL_PROPERTY_RELATION_NAME)
                           .has("dbName", collectionPropertyName).has("clientName", propName)
                           .count().next(),
      is(1L));
  }

  @Test
  public void addPropertyOnlyAddsThePropertyConfigurationForNewProperties() {
    CollectionDescription description = mock(CollectionDescription.class);
    String collectionPropertyName = "propertyName";
    when(description.createPropertyName(anyString())).thenReturn(collectionPropertyName);
    String propName = "propName";
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v.withLabel(DATABASE_LABEL)
                        .withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, ENTITY_NAME)
                        .withProperty(COLLECTION_NAME_PROPERTY_NAME, COLLECTION_NAME))
      .wrap();
    Vertex collectionVertex = graphWrapper.getGraph().traversal().V().next();
    Collection instance = new Collection(VRE_NAME, collectionVertex, graphWrapper, description);
    Vertex entityVertex = mock(Vertex.class);
    String propValue = "propValue";
    Vertex otherVertex = mock(Vertex.class);

    instance.addProperty(entityVertex, propName, propValue);
    instance.addProperty(otherVertex, propName, propValue);

    assertThat(graphWrapper.getGraph().traversal().V(collectionVertex.id())
                           .out(HAS_PROPERTY_RELATION_NAME)
                           .has("dbName", collectionPropertyName).has("clientName", propName)
                           .count().next(),
      is(1L));
  }

  @Test
  public void addPropertyAddsHasNextEdgeBetweenTheProperties() {
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v.withLabel(DATABASE_LABEL)
                        .withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, ENTITY_NAME)
                        .withProperty(COLLECTION_NAME_PROPERTY_NAME, COLLECTION_NAME))
      .wrap();
    Vertex collectionVertex = graphWrapper.getGraph().traversal().V().next();
    Collection instance = new Collection(VRE_NAME, collectionVertex, graphWrapper);

    instance.addProperty(mock(Vertex.class), "prop1", "val1");
    instance.addProperty(mock(Vertex.class), "prop2", "val2");
    instance.addProperty(mock(Vertex.class), "prop3", "val3");

    assertThat(graphWrapper.getGraph().traversal().V(collectionVertex.id())
                           .out(HAS_INITIAL_PROPERTY_RELATION_NAME)
                           .out(HAS_NEXT_PROPERTY_RELATION_NAME)
                           .out(HAS_NEXT_PROPERTY_RELATION_NAME)
                           .count().next(),
      is(1L));
  }

  @Test
  public void addPropertyAddsOnlyOneInitialProperties() {
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v.withLabel(DATABASE_LABEL)
                        .withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, ENTITY_NAME)
                        .withProperty(COLLECTION_NAME_PROPERTY_NAME, COLLECTION_NAME))
      .wrap();
    Vertex collectionVertex = graphWrapper.getGraph().traversal().V().next();
    Collection instance = new Collection(VRE_NAME, collectionVertex, graphWrapper);

    instance.addProperty(mock(Vertex.class), "prop1", "val1");
    instance.addProperty(mock(Vertex.class), "prop2", "val2");

    assertThat(graphWrapper.getGraph().traversal().V(collectionVertex.id())
                           .out(HAS_INITIAL_PROPERTY_RELATION_NAME)
                           .count().next(),
      is(1L));
  }

  @Test
  public void removeRemovesEdgeBetweenTheVertexAndTheCollection() {
    GraphWrapper graphWrapper =
      newGraph().withVertex("collection", v -> v.withLabel(DATABASE_LABEL)
                                                .withOutgoingRelation(HAS_ENTITY_NODE_RELATION_NAME, "entityNode")
                                                .withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, ENTITY_NAME)
                                                .withProperty(COLLECTION_NAME_PROPERTY_NAME, COLLECTION_NAME))
                .withVertex("entityNode", v -> v.withOutgoingRelation(HAS_ENTITY_RELATION_NAME, "entity"))
                .withVertex("entity", v -> v.withLabel("entity"))
                .wrap();
    Vertex collectionVertex = graphWrapper.getGraph().traversal().V().has(T.label, LabelP.of("collection")).next();
    Vertex entityVertex = graphWrapper.getGraph().traversal().V().has(T.label, LabelP.of("entity")).next();
    Collection instance = new Collection(VRE_NAME, collectionVertex, graphWrapper, mock(CollectionDescription.class),
      mock(PropertyHelper.class));

    instance.remove(entityVertex);

    assertThat(graphWrapper.getGraph().traversal().V(collectionVertex.id()).out(HAS_ENTITY_NODE_RELATION_NAME)
                           .out(HAS_ENTITY_RELATION_NAME).hasId(entityVertex.id()).hasNext(), is(false));
  }

  @Test
  public void removeRemovesTheCollectionPropertiesFromTheVertex() {
    GraphWrapper graphWrapper =
      newGraph().withVertex("collection", v -> v.withLabel(DATABASE_LABEL)
                                                .withOutgoingRelation(HAS_ENTITY_NODE_RELATION_NAME, "entityNode")
                                                .withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, ENTITY_NAME)
                                                .withProperty(COLLECTION_NAME_PROPERTY_NAME, COLLECTION_NAME))
                .withVertex("entityNode", v -> v.withOutgoingRelation(HAS_ENTITY_RELATION_NAME, "entity"))
                .withVertex("entity", v -> v.withLabel("entity"))
                .wrap();
    Vertex collectionVertex = graphWrapper.getGraph().traversal().V().has(T.label, LabelP.of("collection")).next();
    Vertex entityVertex = graphWrapper.getGraph().traversal().V().has(T.label, LabelP.of("entity")).next();
    CollectionDescription description = new CollectionDescription(ENTITY_NAME, VRE_NAME);
    PropertyHelper propertyHelper = mock(PropertyHelper.class);
    Collection instance = new Collection(VRE_NAME, collectionVertex, graphWrapper, description, propertyHelper);

    instance.remove(entityVertex);


    verify(propertyHelper).removeProperties(entityVertex, description);
  }

  @Test
  public void removeDoesNothingWhenTheEntityIsNotPartOfTheCollection() {
    GraphWrapper graphWrapper =
      newGraph().withVertex("collection", v -> v.withLabel(DATABASE_LABEL)
                                                .withOutgoingRelation(HAS_ENTITY_NODE_RELATION_NAME, "entityNode")
                                                .withProperty(ENTITY_TYPE_NAME_PROPERTY_NAME, ENTITY_NAME)
                                                .withProperty(COLLECTION_NAME_PROPERTY_NAME, COLLECTION_NAME))
                .withVertex("entityNode", v -> {
                })
                .withVertex("entity", v -> v.withLabel("entity"))
                .wrap();
    Vertex collectionVertex = graphWrapper.getGraph().traversal().V().has(T.label, LabelP.of(DATABASE_LABEL)).next();
    Vertex entityVertex = graphWrapper.getGraph().traversal().V().has(T.label, LabelP.of("entity")).next();
    Collection instance = new Collection(VRE_NAME, collectionVertex, graphWrapper);

    try {
      instance.remove(entityVertex);
    } catch (FastNoSuchElementException e) {
      fail("Should not throw an exception.");
    }
  }

  @Test
  public void equalsReturnsTrueIfTheCollectionDescriptionsAreEqual() {
    CollectionDescription description = new CollectionDescription(ENTITY_NAME, VRE_NAME);
    Collection sameCollection1 = new Collection(null, null, null, description);
    Collection sameCollection2 = new Collection(null, null, null, description);
    Collection otherCollection = new Collection(null, null, null, new CollectionDescription("otherEntity", VRE_NAME));

    assertThat(sameCollection1, is(equalTo(sameCollection2)));
    assertThat(sameCollection1, not(is((equalTo(otherCollection)))));
  }
}
