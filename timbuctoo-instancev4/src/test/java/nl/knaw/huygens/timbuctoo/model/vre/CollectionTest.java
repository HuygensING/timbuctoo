package nl.knaw.huygens.timbuctoo.model.vre;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_DISPLAY_NAME_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_INITIAL_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.CollectionBuilder.timbuctooCollection;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class CollectionTest {
  private Graph graph;

  @Before
  public void setUp() {
    graph = newGraph().build();
  }


  @Test
  public void saveCreatesAVertexForTheCollection() {
    final Vre vre = new Vre("VreName");
    timbuctooCollection("persons", "").build(vre);

    final Vertex result = vre.getCollectionForCollectionName("persons").get().save(graph);

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
    final Vertex existingVertex = graph.addVertex(DATABASE_LABEL);
    existingVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, "persons");
    final Vre vre = new Vre("VreName");
    timbuctooCollection("persons", "").build(vre);

    final Vertex result = vre.getCollectionForCollectionName("persons").get().save(graph);

    assertThat(result, equalTo(existingVertex));
  }

  @Test
  public void saveCreatesARelationToAnEntityHolderVertex() {
    final Vre vre = new Vre("VreName");
    timbuctooCollection("persons", "").build(vre);

    final Vertex result = vre.getCollectionForCollectionName("persons").get().save(graph);

    assertThat(result.vertices(Direction.OUT, Collection.HAS_ENTITY_NODE_RELATION_NAME).next(), likeVertex()
      .withLabel(Collection.COLLECTION_ENTITIES_LABEL)
    );
  }

  @Test
  public void saveDoesNotCreateARelationToAnEntityHolderVertexIfItAlreadyExists() {
    final Vre vre = new Vre("VreName");
    final Vertex existingVertex = graph.addVertex(DATABASE_LABEL);
    final Vertex existingEntityHolderVertex = graph.addVertex(Collection.COLLECTION_ENTITIES_LABEL);
    existingVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, "persons");
    existingVertex.addEdge(Collection.HAS_ENTITY_NODE_RELATION_NAME,
      existingEntityHolderVertex);
    timbuctooCollection("persons", "").build(vre);

    final Vertex result = vre.getCollectionForCollectionName("persons").get().save(graph);

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
    timbuctooCollection("prefixedpersons", "prefixed").build(vre);

    final Vertex result = vre.getCollectionForCollectionName("prefixedpersons").get().save(graph);

    assertThat(result.vertices(Direction.OUT, Collection.HAS_ARCHETYPE_RELATION_NAME).hasNext(), equalTo(true));
  }

  @Test
  public void saveSavesThePropertyConfigurations() {
    final Vre vre = new Vre("VreName");
    timbuctooCollection("persons", "")
      .withProperty("prop1", localProperty("person_prop1"))
      .withProperty("prop2", localProperty("person_prop2"))
      .withProperty("prop3", localProperty("person_prop3"))
      .withDisplayName(localProperty("person_prop1"))
      .build(vre);

    final Vertex result = vre.getCollectionForCollectionName("persons").get().save(graph);
    final List<Vertex> propertyVertices =
      Lists.newArrayList(result.vertices(Direction.OUT, Collection.HAS_PROPERTY_RELATION_NAME));

    assertThat(propertyVertices, containsInAnyOrder(
      likeVertex()
        .withLabel(ReadableProperty.DATABASE_LABEL)
        .withProperty(ReadableProperty.CLIENT_PROPERTY_NAME, "prop1")
        .withProperty(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop1"),

      likeVertex()
        .withLabel(ReadableProperty.DATABASE_LABEL)
        .withProperty(ReadableProperty.CLIENT_PROPERTY_NAME, "prop2")
        .withProperty(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop2"),

      likeVertex()
        .withLabel(ReadableProperty.DATABASE_LABEL)
        .withProperty(ReadableProperty.CLIENT_PROPERTY_NAME, "prop3")
        .withProperty(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop3")
    ));

    assertThat(result.vertices(Direction.OUT, HAS_DISPLAY_NAME_RELATION_NAME).next(),
      likeVertex()
        .withLabel(ReadableProperty.DATABASE_LABEL)
        .withProperty(ReadableProperty.CLIENT_PROPERTY_NAME, ReadableProperty.DISPLAY_NAME_PROPERTY_NAME)
        .withProperty(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop1")
    );

    assertThat(result.vertices(Direction.OUT, HAS_INITIAL_PROPERTY_RELATION_NAME).next(),
      likeVertex()
        .withLabel(ReadableProperty.DATABASE_LABEL)
        .withProperty(ReadableProperty.CLIENT_PROPERTY_NAME, "prop1")
        .withProperty(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop1")
    );
  }

  @Test
  public void saveAddsOrderingRelationsBetweenItsPropertyVerticesToMaintainSortorder() {
    final Vre vre = new Vre("VreName");
    timbuctooCollection("persons", "")
      .withProperty("prop1", localProperty("person_prop1"))
      .withProperty("prop2", localProperty("person_prop2"))
      .withProperty("prop3", localProperty("person_prop3"))
      .withDisplayName(localProperty("person_prop1"))
      .build(vre);

    Vertex current = vre.getCollectionForCollectionName("persons").get().save(graph)
                        .vertices(Direction.OUT, HAS_INITIAL_PROPERTY_RELATION_NAME).next();
    List<Vertex> result = Lists.newArrayList();
    result.add(current);
    while (current.vertices(Direction.OUT, ReadableProperty.HAS_NEXT_PROPERTY_RELATION_NAME).hasNext()) {
      current = current.vertices(Direction.OUT, ReadableProperty.HAS_NEXT_PROPERTY_RELATION_NAME).next();
      result.add(current);
    }

    assertThat(result.size(), equalTo(3));
    assertThat(result, contains(
      likeVertex()
        .withLabel(ReadableProperty.DATABASE_LABEL)
        .withProperty(ReadableProperty.CLIENT_PROPERTY_NAME, "prop1")
        .withProperty(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop1"),

      likeVertex()
        .withLabel(ReadableProperty.DATABASE_LABEL)
        .withProperty(ReadableProperty.CLIENT_PROPERTY_NAME, "prop2")
        .withProperty(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop2"),

      likeVertex()
        .withLabel(ReadableProperty.DATABASE_LABEL)
        .withProperty(ReadableProperty.CLIENT_PROPERTY_NAME, "prop3")
        .withProperty(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop3")
    ));
  }

  @Test
  public void saveDropsRelatedExistingPropertyVertices() {
    final Vre vre = new Vre("VreName");
    final Vertex existingVertex = graph.addVertex(DATABASE_LABEL);
    final Vertex existingPropertyVertex = graph.addVertex(ReadableProperty.DATABASE_LABEL);
    final Vertex existingPropertyVertex2 = graph.addVertex(ReadableProperty.DATABASE_LABEL);
    final Vertex existingPropertyVertex3 = graph.addVertex(ReadableProperty.DATABASE_LABEL);
    existingVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, "persons");
    existingVertex.addEdge(HAS_PROPERTY_RELATION_NAME, existingPropertyVertex);
    existingVertex.addEdge(HAS_DISPLAY_NAME_RELATION_NAME, existingPropertyVertex2);
    existingVertex.addEdge(HAS_INITIAL_PROPERTY_RELATION_NAME, existingPropertyVertex3);
    timbuctooCollection("persons", "").build(vre);

    vre.getCollectionForCollectionName("persons").get().save(graph);

    assertThat(graph.traversal().V().hasLabel(ReadableProperty.DATABASE_LABEL).hasNext(), equalTo(false));
  }

  @Test
  public void saveDoesNotDropUnRelatedExistingPropertyVertices() {
    final Vre vre = new Vre("VreName");
    final Vertex existingVertex = graph.addVertex(DATABASE_LABEL);
    graph.addVertex(ReadableProperty.DATABASE_LABEL);
    existingVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, "persons");
    timbuctooCollection("persons", "").build(vre);

    vre.getCollectionForCollectionName("persons").get().save(graph);

    assertThat(graph.traversal().V().hasLabel(ReadableProperty.DATABASE_LABEL).hasNext(), equalTo(true));
  }
}
