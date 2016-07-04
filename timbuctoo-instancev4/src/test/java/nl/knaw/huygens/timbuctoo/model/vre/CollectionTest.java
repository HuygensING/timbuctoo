package nl.knaw.huygens.timbuctoo.model.vre;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.model.properties.WwDocumentDisplayName;
import nl.knaw.huygens.timbuctoo.model.properties.converters.StringToStringConverter;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.COLLECTION_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.DATABASE_LABEL;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_ARCHETYPE_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_DISPLAY_NAME_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_INITIAL_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.HAS_PROPERTY_RELATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Collection.IS_RELATION_COLLECTION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.CollectionBuilder.timbuctooCollection;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

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
      .withProperty(Collection.IS_RELATION_COLLECTION_PROPERTY_NAME, false)
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

  @Test
  public void loadLoadsACollectionFromAVertex() {
    final Vertex collectionVertex = graph.addVertex(Collection.DATABASE_LABEL);
    final String collectionName = "persons";
    final String entityTypeName = "person";
    collectionVertex.property(COLLECTION_NAME_PROPERTY_NAME, collectionName);
    collectionVertex.property(ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName);
    collectionVertex.property(IS_RELATION_COLLECTION_PROPERTY_NAME, false);

    final Collection instance = Collection.load(collectionVertex, new Vre("dummy"));

    assertThat(instance.getEntityTypeName(), equalTo(entityTypeName));
    assertThat(instance.getCollectionName(), equalTo(collectionName));
    assertThat(instance.getAbstractType(), equalTo(entityTypeName));
    assertThat(instance.isRelationCollection(), equalTo(false));
  }

  @Test
  public void loadLoadsAnInheritingCollectionFromAVertexWithHasArchetypeRelation() {
    final Vertex collectionVertex = graph.addVertex(Collection.DATABASE_LABEL);
    final Vertex abstractCollectionVertex = graph.addVertex(Collection.DATABASE_LABEL);
    final String collectionName = "wwpersons";
    final String entityTypeName = "wwperson";
    final String abstractCollectionName = "persons";
    final String abstractEntityTypeName = "person";
    collectionVertex.property(COLLECTION_NAME_PROPERTY_NAME, collectionName);
    collectionVertex.property(ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName);
    collectionVertex.property(IS_RELATION_COLLECTION_PROPERTY_NAME, false);
    abstractCollectionVertex.property(COLLECTION_NAME_PROPERTY_NAME, abstractCollectionName);
    abstractCollectionVertex.property(ENTITY_TYPE_NAME_PROPERTY_NAME, abstractEntityTypeName);
    collectionVertex.addEdge(HAS_ARCHETYPE_RELATION_NAME, abstractCollectionVertex);

    final Collection instance = Collection.load(collectionVertex, new Vre("dummy"));

    assertThat(instance.getAbstractType(), equalTo(abstractEntityTypeName));
  }

  @Test
  public void loadLoadsTheProperties() {
    final Vertex collectionVertex = graph.addVertex(Collection.DATABASE_LABEL);
    final Vertex prop1Vertex = graph.addVertex(ReadableProperty.DATABASE_LABEL);
    final Vertex prop2Vertex = graph.addVertex(ReadableProperty.DATABASE_LABEL);
    final Vertex prop3Vertex = graph.addVertex(ReadableProperty.DATABASE_LABEL);
    final String collectionName = "persons";
    final String entityTypeName = "person";
    final String propertyType = new StringToStringConverter().getUniqueTypeIdentifier();
    collectionVertex.property(COLLECTION_NAME_PROPERTY_NAME, collectionName);
    collectionVertex.property(ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName);
    collectionVertex.property(IS_RELATION_COLLECTION_PROPERTY_NAME, false);
    prop1Vertex.property(LocalProperty.CLIENT_PROPERTY_NAME, "prop1");
    prop1Vertex.property(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop1");

    prop1Vertex.property(LocalProperty.PROPERTY_TYPE_NAME, propertyType);
    prop2Vertex.property(LocalProperty.CLIENT_PROPERTY_NAME, "prop2");
    prop2Vertex.property(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop2");
    prop2Vertex.property(LocalProperty.PROPERTY_TYPE_NAME, propertyType);
    prop3Vertex.property(LocalProperty.CLIENT_PROPERTY_NAME, "prop3");
    prop3Vertex.property(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop3");
    prop3Vertex.property(LocalProperty.PROPERTY_TYPE_NAME, "encoded-string-of-limited-values");
    prop3Vertex.property(LocalProperty.OPTIONS_PROPERTY_NAME, "[\"a\", \"b\"]");
    collectionVertex.addEdge(HAS_INITIAL_PROPERTY_RELATION_NAME, prop1Vertex);
    prop1Vertex.addEdge(LocalProperty.HAS_NEXT_PROPERTY_RELATION_NAME, prop2Vertex);
    prop2Vertex.addEdge(LocalProperty.HAS_NEXT_PROPERTY_RELATION_NAME, prop3Vertex);

    final Collection instance = Collection.load(collectionVertex, new Vre("dummy"));


    assertThat(instance.getWriteableProperties().keySet(), contains(
      "prop1", "prop2", "prop3"
    ));

    assertThat(instance.getWriteableProperties().values(), contains(
      instanceOf(ReadableProperty.class),
      instanceOf(ReadableProperty.class),
      instanceOf(ReadableProperty.class)
    ));
  }

  @Test
  public void loadLoadsTheDisplayName() {
    final Vertex collectionVertex = graph.addVertex(Collection.DATABASE_LABEL);
    final Vertex displayNameVertex = graph.addVertex(ReadableProperty.DATABASE_LABEL);
    final String collectionName = "persons";
    final String entityTypeName = "person";
    final String propertyType = new StringToStringConverter().getUniqueTypeIdentifier();
    collectionVertex.property(COLLECTION_NAME_PROPERTY_NAME, collectionName);
    collectionVertex.property(ENTITY_TYPE_NAME_PROPERTY_NAME, entityTypeName);
    collectionVertex.property(IS_RELATION_COLLECTION_PROPERTY_NAME, false);
    displayNameVertex.property(LocalProperty.CLIENT_PROPERTY_NAME, "@displayName");
    displayNameVertex.property(LocalProperty.DATABASE_PROPERTY_NAME, "person_prop1");
    displayNameVertex.property(LocalProperty.PROPERTY_TYPE_NAME, propertyType);

    collectionVertex.addEdge(HAS_DISPLAY_NAME_RELATION_NAME, displayNameVertex);

    final Collection instance = Collection.load(collectionVertex, new Vre("dummy"));

    assertThat(instance.getDisplayName(), instanceOf(ReadableProperty.class));
  }

  /*
    final Vertex archetype = collectionVertex.vertices(Direction.OUT, HAS_ARCHETYPE_RELATION_NAME).hasNext() ?
      collectionVertex.vertices(Direction.OUT, HAS_ARCHETYPE_RELATION_NAME).next() :
      null;

    final String entityTypeName = collectionVertex.value(ENTITY_TYPE_NAME_PROPERTY_NAME);
    final String abstractType = archetype == null ? entityTypeName : archetype.value(ENTITY_TYPE_NAME_PROPERTY_NAME);
    final String collectionName = collectionVertex.value(COLLECTION_NAME_PROPERTY_NAME);

    final ReadableProperty displayName = null; // TODO
    final LinkedHashMap<String, ReadableProperty> properties = Maps.newLinkedHashMap(); //  TODO
    final Vre vre = null; // TODO
    final Map<String, Supplier<GraphTraversal<Object, Vertex>>> derivedRelations = null; // FIXME: not functionally used
    boolean isRelationCollection = collectionVertex.value(IS_RELATION_COLLECTION_PROPERTY_NAME);

    // String entityTypeName
    // String abstractType,
    // ReadableProperty displayName,
    // LinkedHashMap<String, ReadableProperty> properties,
    // String collectionName,
    // Vre vre,
    // Map<String, Supplier<GraphTraversal<Object, Vertex>>> derivedRelations,
    // boolean isRelationCollection

    return new Collection(entityTypeName, abstractType, displayName, properties, collectionName, vre, derivedRelations,
      isRelationCollection);
 */
}
