package nl.knaw.huygens.timbuctoo.database;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.dto.property.StringProperty;
import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
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
        )).build();
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

}
