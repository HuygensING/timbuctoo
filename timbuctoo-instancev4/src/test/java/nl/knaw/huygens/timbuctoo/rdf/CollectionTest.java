package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty.HAS_NEXT_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_INITIAL_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

}
