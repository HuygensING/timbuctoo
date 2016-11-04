package nl.knaw.huygens.timbuctoo.database;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.crud.GremlinEntityFetcher;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.CollectionBuilder;
import nl.knaw.huygens.timbuctoo.database.dto.property.StringProperty;
import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class DataStoreOperationsTest {

  @Test
  public void emptyDatabaseIsShownAsEmpty() throws Exception {
    DataStoreOperations instance = new DataStoreOperations(newGraph().wrap(), null, null, null);

    boolean isEmpty = instance.databaseIsEmptyExceptForMigrations();

    assertThat(isEmpty, is(true));
  }

  @Test
  public void nonEmptyDatabaseIsShownAsFull() throws Exception {
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withTimId(UUID.randomUUID().toString())
      ).wrap();
    DataStoreOperations instance = new DataStoreOperations(graphWrapper, null, null, null);

    boolean isEmpty = instance.databaseIsEmptyExceptForMigrations();

    assertThat(isEmpty, is(false));
  }

  @Test
  public void ensureVreExistsCreatesAVreIfNeeded() throws Exception {
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withTimId(UUID.randomUUID().toString())
      ).wrap();
    DataStoreOperations instance = new DataStoreOperations(graphWrapper, null, null, null);

    instance.ensureVreExists("SomeVre");
    assertThat(
      graphWrapper.getGraph().traversal().V()
                  .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
                  .has(Vre.VRE_NAME_PROPERTY_NAME, "SomeVre")
                  .hasNext(),
      is(true)
    );
  }

  @Test
  public void createEntityAddsAnEntityWithItsPropertiesToTheDatabase() throws Exception {
    GraphWrapper graphWrapper = newGraph().wrap();
    Vres vres = createConfiguration();
    final Collection collection = vres.getCollection("testthings").get();
    final DataStoreOperations instance = new DataStoreOperations(graphWrapper, mock(ChangeListener.class), null, vres);
    List<TimProperty<?>> properties = Lists.newArrayList();
    properties.add(new StringProperty("prop1", "val1"));
    properties.add(new StringProperty("prop2", "val2"));
    CreateEntity createEntity = new CreateEntity(properties);
    UUID id = UUID.randomUUID();
    createEntity.setId(id);
    createEntity.setCreated(new Change(Instant.now().toEpochMilli(), "userId", null));

    instance.createEntity(collection, Optional.empty(), createEntity);

    assertThat(graphWrapper.getGraph()
                           .traversal().V()
                           .has("tim_id", id.toString())
                           .has("testthing_prop1", "val1")
                           .has("testthing_prop2", "val2")
                           .hasNext(),
      is(true));
  }

  @Test // TODO move the TimbuctooActions
  public void createEntitySetsTheRevToOne() throws Exception {
    GraphWrapper graphWrapper = newGraph().wrap();
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    DataStoreOperations instance = new DataStoreOperations(graphWrapper, mock(ChangeListener.class), null, vres);
    List<TimProperty<?>> properties = Lists.newArrayList();
    CreateEntity createEntity = new CreateEntity(properties);
    UUID id = UUID.randomUUID();
    createEntity.setId(id);
    createEntity.setCreated(new Change(Instant.now().toEpochMilli(), "userId", null));

    instance.createEntity(collection, Optional.empty(), createEntity);

    assertThat(graphWrapper.getGraph()
                           .traversal().V()
                           .has("tim_id", id.toString())
                           .has("rev", 1)
                           .hasNext(),
      is(true));
  }

  @Test
  public void createEntitySetsTypesWithTheCollectionAndTheBaseCollection() throws Exception {
    GraphWrapper graphWrapper = newGraph().wrap();
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    DataStoreOperations instance = new DataStoreOperations(graphWrapper, mock(ChangeListener.class), null, vres);
    List<TimProperty<?>> properties = Lists.newArrayList();
    CreateEntity createEntity = new CreateEntity(properties);
    UUID id = UUID.randomUUID();
    createEntity.setId(id);
    createEntity.setCreated(new Change(Instant.now().toEpochMilli(), "userId", null));

    instance.createEntity(collection, Optional.empty(), createEntity);

    assertThat(graphWrapper.getGraph()
                           .traversal().V()
                           .has("tim_id", id.toString())
                           .next().value("types"),
      allOf(containsString("testthing"), containsString("thing"))
    );
  }

  private Vres createConfiguration() {
    return new VresBuilder()
      .withVre("testVre", "test", vre -> vre
        .withCollection("testthings", col -> col
          .withProperty("prop1", localProperty("testthing_prop1"))
          .withProperty("prop2", localProperty("testthing_prop2"))
        )
        .withCollection("teststuffs")
        .withCollection("testrelations", CollectionBuilder::isRelationCollection)
      )
      .withVre("otherVre", "other", vre -> vre
        .withCollection("otherthings", col -> col
          .withProperty("prop1", localProperty("otherthing_prop1"))
          .withProperty("prop2", localProperty("otherthing_prop2"))
        ))
      .build();
  }

  @Test
  public void createEntitySetsCreatedAndModified() throws Exception {
    GraphWrapper graphWrapper = newGraph().wrap();
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    DataStoreOperations instance = new DataStoreOperations(graphWrapper, mock(ChangeListener.class), null, vres);
    List<TimProperty<?>> properties = Lists.newArrayList();
    CreateEntity createEntity = new CreateEntity(properties);
    UUID id = UUID.randomUUID();
    createEntity.setId(id);
    String userId = "userId";
    long timeStamp = Instant.now().toEpochMilli();
    createEntity.setCreated(new Change(timeStamp, userId, null));

    instance.createEntity(collection, Optional.empty(), createEntity);

    assertThat(graphWrapper.getGraph()
                           .traversal().V()
                           .has("tim_id", id.toString())
                           .next().value("created"),
      sameJSONAs(String.format("{\"timeStamp\": %s,\"userId\": \"%s\"}", timeStamp, userId))
    );

    assertThat(graphWrapper.getGraph()
                           .traversal().V()
                           .has("tim_id", id.toString())
                           .next().value("modified"),
      sameJSONAs(String.format("{\"timeStamp\": %s,\"userId\": \"%s\"}", timeStamp, userId))
    );
  }

  @Test
  public void createEntityDuplicatesTheVertex() throws Exception {
    GraphWrapper graphWrapper = newGraph().wrap();
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    DataStoreOperations instance = new DataStoreOperations(graphWrapper, mock(ChangeListener.class), null, vres);
    List<TimProperty<?>> properties = Lists.newArrayList();
    CreateEntity createEntity = new CreateEntity(properties);
    UUID id = UUID.randomUUID();
    createEntity.setId(id);
    String userId = "userId";
    long timeStamp = Instant.now().toEpochMilli();
    createEntity.setCreated(new Change(timeStamp, userId, null));

    instance.createEntity(collection, Optional.empty(), createEntity);

    assertThat(graphWrapper.getGraph().traversal().V().has("tim_id", id.toString()).count().next(), is(2L));
  }

  @Test
  public void createEntityNotifiesTheChangeListener() throws Exception {
    GraphWrapper graphWrapper = newGraph().wrap();
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    ChangeListener changeListener = mock(ChangeListener.class);
    DataStoreOperations instance = new DataStoreOperations(graphWrapper, changeListener, null, vres);
    List<TimProperty<?>> properties = Lists.newArrayList();
    CreateEntity createEntity = new CreateEntity(properties);
    UUID id = UUID.randomUUID();
    createEntity.setId(id);
    String userId = "userId";
    long timeStamp = Instant.now().toEpochMilli();
    createEntity.setCreated(new Change(timeStamp, userId, null));

    instance.createEntity(collection, Optional.empty(), createEntity);

    Vertex vertex =
      graphWrapper.getGraph().traversal().V().has("tim_id", id.toString()).in("VERSION_OF").next();
    verify(changeListener).onCreate(vertex);
  }

  @Test
  public void createEntityMarksOneVertexAsLatest() throws Exception {
    GraphWrapper graphWrapper = newGraph().wrap();
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    ChangeListener changeListener = mock(ChangeListener.class);
    DataStoreOperations instance = new DataStoreOperations(graphWrapper, changeListener, null, vres);
    List<TimProperty<?>> properties = Lists.newArrayList();
    CreateEntity createEntity = new CreateEntity(properties);
    UUID id = UUID.randomUUID();
    createEntity.setId(id);
    String userId = "userId";
    long timeStamp = Instant.now().toEpochMilli();
    createEntity.setCreated(new Change(timeStamp, userId, null));

    instance.createEntity(collection, Optional.empty(), createEntity);


    assertThat(graphWrapper.getGraph()
                           .traversal().V()
                           .has("tim_id", id.toString())
                           .has("isLatest", true)
                           .count().next(),
      is(1L));

  }

  @Test(expected = IOException.class)
  public void createEntityThrowsAnIoExceptionWhenItEncountersAnUnknownProperty() throws Exception {
    GraphWrapper graphWrapper = newGraph().wrap();
    Vres vres = createConfiguration();
    final Collection collection = vres.getCollection("testthings").get();
    final DataStoreOperations instance = new DataStoreOperations(graphWrapper, mock(ChangeListener.class), null, vres);
    List<TimProperty<?>> properties = Lists.newArrayList();
    properties.add(new StringProperty("prop1", "val1"));
    properties.add(new StringProperty("unknowProp", "val2"));
    CreateEntity createEntity = new CreateEntity(properties);
    UUID id = UUID.randomUUID();
    createEntity.setId(id);
    createEntity.setCreated(new Change(Instant.now().toEpochMilli(), "userId", null));

    instance.createEntity(collection, Optional.empty(), createEntity);
  }

  // TODO move increase of the rev to TimbuctooActions
  @Test
  public void deleteEntityIncreasesTheRevision() throws Exception {
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    UUID id = UUID.randomUUID();
    String idString = id.toString();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withTimId(idString)
        .withProperty("isLatest", true)
        .withVre("test")
        .withType("thing")
        .withProperty("rev", 1)
        .withIncomingRelation("VERSION_OF", "orig")
      )
      .withVertex("orig", v -> v
        .withTimId(idString)
        .withVre("test")
        .withType("thing")
        .withProperty("isLatest", false)
        .withProperty("rev", 1)
      )
      .wrap();
    GremlinEntityFetcher entityFetcher = new GremlinEntityFetcher();
    DataStoreOperations instance =
      new DataStoreOperations(graphWrapper, mock(ChangeListener.class), entityFetcher, vres);

    int rev = instance.deleteEntity(collection, id, new Change(Instant.now().toEpochMilli(), "userId", null));

    assertThat(rev, is(2));
  }

  @Test
  public void deleteEntityRemovesTypeWhenOtherTypesExist() throws Exception {
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    UUID id = UUID.randomUUID();
    String idString = id.toString();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withTimId(idString)
        .withVre("test")
        .withVre("other")
        .withType("thing")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withLabel("testthing")
        .withLabel("otherthing")
      )
      .wrap();
    GremlinEntityFetcher entityFetcher = new GremlinEntityFetcher();
    DataStoreOperations instance =
      new DataStoreOperations(graphWrapper, mock(ChangeListener.class), entityFetcher, vres);

    instance.deleteEntity(collection, id, new Change(Instant.now().toEpochMilli(), "userId", null));

    String types = (String) graphWrapper.getGraph().traversal().V()
                                        .has("tim_id", idString)
                                        .has("isLatest", true)
                                        .properties("types").value()
                                        .next();

    assertThat(types, is("[\"otherthing\"]"));

    // Type should also be removed from the Neo4j labels
    assertThat(graphWrapper.getGraph().traversal().V()
                           .has("tim_id", idString)
                           .has("isLatest", true)
                           .has(T.label, LabelP.of("testthing")).hasNext(), is(false));

    // Other type should not be removed from the Neo4j labels
    assertThat(graphWrapper.getGraph().traversal().V()
                           .has("tim_id", idString)
                           .has("isLatest", true)
                           .has(T.label, LabelP.of("otherthing")).hasNext(), is(true));
  }

  @Test
  public void deleteEntitySetsDeletedToTrueWhenLastTypeIsRemoved() throws Exception {
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    UUID id = UUID.randomUUID();
    String idString = id.toString();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withTimId(idString)
        .withProperty("isLatest", true)
        .withVre("test")
        .withType("thing")
        .withProperty("rev", 1)
        .withIncomingRelation("VERSION_OF", "orig")
      )
      .withVertex("orig", v -> v
        .withTimId(idString)
        .withVre("test")
        .withType("thing")
        .withProperty("isLatest", false)
        .withProperty("rev", 1)
      )
      .wrap();
    GremlinEntityFetcher entityFetcher = new GremlinEntityFetcher();
    DataStoreOperations instance =
      new DataStoreOperations(graphWrapper, mock(ChangeListener.class), entityFetcher, vres);

    instance.deleteEntity(collection, id, new Change(Instant.now().toEpochMilli(), "userId", null));

    assertThat(graphWrapper.getGraph().traversal().V()
                           .has("tim_id", idString)
                           .has("deleted", true).hasNext(), is(true));
  }

  @Test
  public void deleteEntitySetsModified() throws Exception {
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    UUID id = UUID.randomUUID();
    String idString = id.toString();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withTimId(idString)
        .withProperty("isLatest", true)
        .withVre("test")
        .withType("thing")
        .withProperty("rev", 1)
        .withIncomingRelation("VERSION_OF", "orig")
      )
      .withVertex("orig", v -> v
        .withTimId(idString)
        .withVre("test")
        .withType("thing")
        .withProperty("isLatest", false)
        .withProperty("rev", 1)
      )
      .wrap();
    GremlinEntityFetcher entityFetcher = new GremlinEntityFetcher();
    DataStoreOperations instance =
      new DataStoreOperations(graphWrapper, mock(ChangeListener.class), entityFetcher, vres);
    long timeStamp = Instant.now().toEpochMilli();
    String userId = "userId";

    instance.deleteEntity(collection, id, new Change(timeStamp, userId, null));

    assertThat(graphWrapper.getGraph().traversal().V()
                           .has("tim_id", idString)
                           .has("deleted", true).next()
                           .value("modified"),
      sameJSONAs(String.format("{\"timeStamp\": %s,\"userId\": \"%s\"}", timeStamp, userId)));
  }

  @Test
  public void deleteEntityPreparesBackupCopyAfterMakingChanges() throws Exception {
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    UUID id = UUID.randomUUID();
    String idString = id.toString();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withTimId(idString)
        .withVre("test")
        .withType("thing")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withIncomingRelation("VERSION_OF", "orig")
      )
      .withVertex("orig", v -> v
        .withTimId(idString)
        .withVre("test")
        .withType("thing")
        .withProperty("isLatest", false)
        .withProperty("rev", 1)
      )
      .wrap();
    GremlinEntityFetcher entityFetcher = new GremlinEntityFetcher();
    DataStoreOperations instance =
      new DataStoreOperations(graphWrapper, mock(ChangeListener.class), entityFetcher, vres);


    Vertex beforeUpdate = graphWrapper.getGraph().traversal().V()
                                      .has("tim_id", idString)
                                      .has("isLatest", true)
                                      .next();

    instance.deleteEntity(collection, id, new Change(Instant.now().toEpochMilli(), "userId", null));

    Vertex afterUpdate = graphWrapper.getGraph().traversal().V()
                                     .has("tim_id", idString)
                                     .has("isLatest", true)
                                     .next();

    assertThat(afterUpdate.id(), is(not(beforeUpdate.id())));
    //single edge, containing the VERSION_OF pointer
    assertThat(afterUpdate.edges(Direction.IN).next().outVertex().id(), is(beforeUpdate.id()));
  }

  @Test
  public void deleteEntityNotifiesTheChangeListenerBeforeDuplicatingTheVertex() throws Exception {
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    UUID id = UUID.randomUUID();
    String idString = id.toString();
    GraphWrapper graphWrapper = newGraph()
      .withVertex("orig", v -> v
        .withTimId(idString)
        .withVre("test")
        .withType("thing")
        .withProperty("isLatest", false)
        .withProperty("rev", 1)
      )
      .withVertex(v -> v
        .withTimId(idString)
        .withProperty("isLatest", true)
        .withVre("test")
        .withType("thing")
        .withProperty("rev", 1)
        .withIncomingRelation("VERSION_OF", "orig")
      ).wrap();
    GremlinEntityFetcher entityFetcher = new GremlinEntityFetcher();
    ChangeListener changeListener = mock(ChangeListener.class);
    DataStoreOperations instance = new DataStoreOperations(graphWrapper, changeListener, entityFetcher, vres);

    instance.deleteEntity(collection, id, new Change(Instant.now().toEpochMilli(), "userId", null));

    Vertex latestAfterDuplication = graphWrapper.getGraph()
                                                .traversal().V()
                                                .has("tim_id", idString).has("isLatest", true).next();
    Vertex newVertex = latestAfterDuplication.vertices(Direction.IN, "VERSION_OF").next();
    Vertex oldVertex = newVertex.vertices(Direction.IN, "VERSION_OF").next();
    verify(changeListener).onUpdate(Optional.of(oldVertex), newVertex);
  }

  @Test
  public void deleteEntityMovesRelationsToNewestVertex() throws Exception {
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    UUID id = UUID.randomUUID();
    String idString = id.toString();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withTimId(idString)
        .withVre("test")
        .withType("thing")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withOutgoingRelation("hasWritten", "stuff")
        .withIncomingRelation("isFriendOf", "friend")
      )
      .withVertex("stuff", v -> v
        .withVre("test")
        .withType("stuff")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .withVertex("friend", v -> v
        .withVre("test")
        .withType("thing")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .wrap();
    GremlinEntityFetcher entityFetcher = new GremlinEntityFetcher();
    DataStoreOperations instance =
      new DataStoreOperations(graphWrapper, mock(ChangeListener.class), entityFetcher, vres);
    Vertex orig = graphWrapper.getGraph().traversal().V().has("tim_id", idString).has("isLatest", true).next();
    assertThat(stream(orig.edges(Direction.BOTH, "hasWritten", "isFriendOf")).count(), is(2L));

    instance.deleteEntity(collection, id, new Change(Instant.now().toEpochMilli(), "userId", null));

    Vertex replacement = graphWrapper.getGraph().traversal().V().has("tim_id", idString).has("isLatest", true).next();
    assertThat(stream(orig.edges(Direction.BOTH, "hasWritten", "isFriendOf")).count(), is(0L));
    assertThat(stream(replacement.edges(Direction.BOTH, "hasWritten", "isFriendOf")).count(), is(2L));
  }

  @Test
  public void deletesAllRelationsOfCurrentVre() throws Exception {
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    UUID id = UUID.randomUUID();
    String idString = id.toString();
    final String testOnlyId = "10000000-0000-0000-0000-000000000000";
    final String otherOnlyId = "20000000-0000-0000-0000-000000000000";
    final String inBothId = "30000000-0000-0000-0000-000000000000";
    GraphWrapper graphManager = newGraph()
      .withVertex(v -> v
        .withTimId(idString)
        .withVre("test")
        .withType("thing")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withOutgoingRelation("hasWritten", "stuff", rel -> rel
          .withTim_id(UUID.fromString(testOnlyId))
          .removeType("other")
          .withAccepted("testrelation", true)
        )
        .withIncomingRelation("isFriendOf", "friend", rel -> rel
          .withTim_id(UUID.fromString(inBothId))
          .withAccepted("testrelation", true)
          .withAccepted("otherrelation", true)
        )
        .withIncomingRelation("isFriendOf", "friend", rel -> rel
          .withTim_id(UUID.fromString(otherOnlyId))
          .removeType("test")
          .withAccepted("otherrelation", true)
        )
      )
      .withVertex("stuff", v -> v
        .withVre("test")
        .withType("stuff")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .withVertex("friend", v -> v
        .withVre("test")
        .withVre("other")
        .withType("thing")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .wrap();

    GremlinEntityFetcher entityFetcher = new GremlinEntityFetcher();
    DataStoreOperations instance =
      new DataStoreOperations(graphManager, mock(ChangeListener.class), entityFetcher, vres);

    instance.deleteEntity(collection, id, new Change(Instant.now().toEpochMilli(), "userId", null));

    assertThat(graphManager.getGraph().traversal().E()
                           .has("tim_id", testOnlyId)
                           .has("isLatest", true)
                           .has("testrelation_accepted", false).not(has("otherrelation_accepted"))
                           .hasNext(), is(true));
    assertThat(graphManager.getGraph().traversal().E()
                           .has("tim_id", inBothId)
                           .has("isLatest", true)
                           .has("testrelation_accepted", false).has("otherrelation_accepted", true)
                           .hasNext(), is(true));
    assertThat(graphManager.getGraph().traversal().E()
                           .has("tim_id", otherOnlyId)
                           .has("isLatest", true)
                           .not(has("testrelation_accepted")).has("otherrelation_accepted", true)
                           .hasNext(), is(true));

  }

  @Test(expected = NotFoundException.class)
  public void deleteEntityThrowsNotFoundWhenTheEntityIsNotOfThisVre() throws Exception {
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    UUID id = UUID.randomUUID();
    String idString = id.toString();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withTimId(idString)
        .withVre("other")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("deleted", false)
      )
      .wrap();
    GremlinEntityFetcher entityFetcher = new GremlinEntityFetcher();
    DataStoreOperations instance =
      new DataStoreOperations(graphWrapper, mock(ChangeListener.class), entityFetcher, vres);

    instance.deleteEntity(collection, id, null);
  }

  @Test(expected = NotFoundException.class)
  public void deleteEntitythrowsNotFoundWhenTheIdIsNotInTheDatabase() throws Exception {
    Vres vres = createConfiguration();
    Collection collection = vres.getCollection("testthings").get();
    UUID id = UUID.randomUUID();
    GraphWrapper graphWrapper = newGraph().wrap();
    GremlinEntityFetcher entityFetcher = new GremlinEntityFetcher();
    DataStoreOperations instance =
      new DataStoreOperations(graphWrapper, mock(ChangeListener.class), entityFetcher, vres);

    instance.deleteEntity(collection, id, null);
  }

}
