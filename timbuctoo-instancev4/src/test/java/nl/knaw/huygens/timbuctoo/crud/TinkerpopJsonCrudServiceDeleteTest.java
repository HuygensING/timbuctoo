package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.TestGraphRule;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class TinkerpopJsonCrudServiceDeleteTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public TestGraphRule testGraph = new TestGraphRule();

  public TinkerpopJsonCrudService basicInstance(Graph graph) {
    return customInstanceMaker(graph, null, null, null, null);
  }

  public TinkerpopJsonCrudService basicInstanceWithClock(Graph graph, Clock clock) {
    return customInstanceMaker(graph, null, null, clock, null);
  }

  private TinkerpopJsonCrudService basicInstanceWithUrlGenerator(Graph graph, UrlGenerator urlGen, HandleAdder adder) {
    return customInstanceMaker(graph, null, urlGen, null, adder);
  }

  private TinkerpopJsonCrudService customInstanceMaker(Graph graph, Vres map,
                                                       UrlGenerator generator, Clock clock, HandleAdder handleAdder) {
    if (map == null) {
      map = new Vres.Builder()
        .withVre("WomenWriters", "ww", vre -> vre
          .withCollection("wwpersons")
          .withCollection("wwrelations")
        )
        .build();
    }
    if (generator == null) {
      generator = (collection, id, rev) -> URI.create("http://example.com/");
    }
    if (clock == null) {
      clock = Clock.systemDefaultZone();
    }
    if (handleAdder == null) {
      handleAdder = mock(HandleAdder.class);
    }

    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    when(graphWrapper.getGraph()).thenReturn(graph);

    return new TinkerpopJsonCrudService(graphWrapper, map, handleAdder, null, generator, generator, generator, clock);
  }

  @Test
  public void throwsOnUnknownMappings() throws Exception {
    Graph graph = testGraph.newGraph().build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(InvalidCollectionException.class);

    instance.delete("not_wwpersons", null, "");
  }

  @Test
  public void increasesRevisionByOne() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = testGraph.newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withProperty("isLatest", true)
        .withVre("ww")
        .withType("person")
        .withProperty("rev", 1)
        .withIncomingRelation("VERSION_OF", "orig")
      )
      .withVertex("orig", v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", false)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.delete("wwpersons", UUID.fromString(id), "");

    int rev = (int) graph.traversal().V().has("tim_id", id).has("isLatest", true).properties("rev").value().next();

    assertThat(rev, is(2));
  }

  @Test
  public void removesTypeWhenOtherTypesExist() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = testGraph.newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ckcc")
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withLabel("wwperson")
        .withLabel("ckccperson")
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.delete("wwpersons", UUID.fromString(id), "");

    String types = (String) graph.traversal().V()
      .has("tim_id", id)
      .has("isLatest", true)
      .properties("types").value()
      .next();

    assertThat(types, is("[\"ckccperson\"]"));

    // Type should also be removed from the Neo4j labels
    assertThat(graph.traversal().V()
            .has("tim_id", id)
            .has("isLatest", true)
            .has(T.label, LabelP.of("wwperson")).hasNext(), is(false));

    // Other type should not be removed from the Neo4j labels
    assertThat(graph.traversal().V()
            .has("tim_id", id)
            .has("isLatest", true)
            .has(T.label, LabelP.of("ckccperson")).hasNext(), is(true));
  }

  @Test
  public void setsDeletedToTrueWhenLastTypeIsRemoved() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = testGraph.newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("deleted", false)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.delete("wwpersons", UUID.fromString(id), "");

    String types = (String) graph.traversal().V()
      .has("tim_id", id)
      .has("isLatest", true)
      .properties("types").value()
      .next();
    assertThat(types, is("[\"wwperson\"]"));


    boolean deleted = (boolean) graph.traversal().V()
      .has("tim_id", id)
      .has("isLatest", true)
      .properties("deleted").value()
      .next();
    assertThat(deleted, is(true));
  }

  @Test
  public void setsModified() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = testGraph.newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();

    int oneSecondPast1970 = 1000;
    TinkerpopJsonCrudService instance = basicInstanceWithClock(
      graph,
      Clock.fixed(Instant.ofEpochMilli(oneSecondPast1970), ZoneId.systemDefault())
    );

    instance.delete("wwpersons", UUID.fromString(id), "despicable_me");
    String modified = (String) graph.traversal().V()
      .has("tim_id", id)
      .has("isLatest", true)
      .properties("modified").value()
      .next();

    MatcherAssert.assertThat(
      modified,
      sameJSONAs(String.format("{\"timeStamp\": %s,\"userId\": \"%s\"}", oneSecondPast1970, "despicable_me"))
    );

  }

  @Test
  public void preparesBackupCopyAfterMakingChanges() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = testGraph.newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withIncomingRelation("VERSION_OF", "orig")
      )
      .withVertex("orig", v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", false)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    Vertex beforeUpdate = graph.traversal().V()
      .has("tim_id", id)
      .has("isLatest", true)
      .next();

    instance.delete("wwpersons", UUID.fromString(id), "");

    Vertex afterUpdate = graph.traversal().V()
      .has("tim_id", id)
      .has("isLatest", true)
      .next();

    assertThat(afterUpdate.id(), is(not(beforeUpdate.id())));
    //single edge, containing the VERSION_OF pointer
    assertThat(afterUpdate.edges(Direction.IN).next().outVertex().id(), is(beforeUpdate.id()));
  }

  @Test
  public void commitsChangesIfEverythingSucceeds() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = testGraph.newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);
    instance.delete("wwpersons", UUID.fromString(id), "");

    graph.tx().close();

    assertThat(graph.traversal().V().toList().size(), is(2));
  }

  @Test
  public void addsPersistentId() throws Exception {
    String uuid = UUID.randomUUID().toString();
    int oldRev = 3;
    Graph graph = testGraph.newGraph()
      .withVertex(v -> v
        .withTimId(uuid)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", oldRev)
      )
      .build();

    HandleAdder handleAdder = mock(HandleAdder.class);
    TinkerpopJsonCrudService instance = basicInstanceWithUrlGenerator(
      graph,
      (collectionName, id, rev) -> URI.create("http://example.com/" + id + "?r=" + rev),
      handleAdder
    );

    instance.delete("wwpersons", UUID.fromString(uuid), "");

    verify(handleAdder, times(1)).add(
      new HandleAdderParameters(
        UUID.fromString(uuid),
        oldRev + 1,
        URI.create("http://example.com/" + uuid + "?r=" + (oldRev + 1))
      )
    );
  }

  @Test
  public void movesRelationsToNewestVertex() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = testGraph.newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withOutgoingRelation("hasWritten", "document")
        .withIncomingRelation("isFriendOf", "friend")
      )
      .withVertex("document", v -> v
        .withVre("ww")
        .withType("document")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .withVertex("friend", v -> v
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    Vertex orig = graph.traversal().V().has("tim_id", id).has("isLatest", true).next();
    assertThat(stream(orig.edges(Direction.BOTH, "hasWritten", "isFriendOf")).count(), is(2L));

    instance.delete("wwpersons", UUID.fromString(id), "");

    Vertex replacement = graph.traversal().V().has("tim_id", id).has("isLatest", true).next();
    assertThat(stream(orig.edges(Direction.BOTH, "hasWritten", "isFriendOf")).count(), is(0L));
    assertThat(stream(replacement.edges(Direction.BOTH, "hasWritten", "isFriendOf")).count(), is(2L));
  }

  @Test
  public void deletesAllRelationsOfCurrentVre() throws Exception {
    String id = UUID.randomUUID().toString();
    final String wwOnlyId = "10000000-0000-0000-0000-000000000000";
    final String ckccOnlyId = "20000000-0000-0000-0000-000000000000";
    final String inBothId = "30000000-0000-0000-0000-000000000000";
    Graph graph = testGraph.newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withOutgoingRelation("hasWritten", "document", rel->rel
          .withTim_id(UUID.fromString(wwOnlyId))
          .removeType("ckcc")
          .withAccepted("wwrelation", true)
        )
        .withIncomingRelation("isFriendOf", "friend", rel->rel
          .withTim_id(UUID.fromString(inBothId))
          .withAccepted("wwrelation", true)
          .withAccepted("ckccrelation", true)
        )
        .withIncomingRelation("isFriendOf", "friend", rel -> rel
          .withTim_id(UUID.fromString(ckccOnlyId))
          .removeType("ww")
          .withAccepted("ckccrelation", true)
        )
      )
      .withVertex("document", v -> v
        .withVre("ww")
        .withType("document")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .withVertex("friend", v -> v
        .withVre("ww")
        .withVre("ckcc")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.delete("wwpersons", UUID.fromString(id), "");

    Vertex replacement = graph.traversal().V().has("tim_id", id).has("isLatest", true).next();

    assertThat(stream(replacement.edges(Direction.BOTH, "hasWritten", "isFriendOf")).count(), is(3L));
    replacement.edges(Direction.BOTH, "hasWritten", "isFriendOf").forEachRemaining( edge -> {
      //System.out.println(edge.id());
      //edge.properties().forEachRemaining(p -> System.out.println("  " + p.key() + ": " + p.value()));

      switch (edge.value("tim_id").toString()) {
        case inBothId:
          assertThat(edge.value("wwrelation_accepted"), is(false));
          assertThat(edge.value("ckccrelation_accepted"), is(true));
          break;
        case wwOnlyId:
          assertThat(edge.value("wwrelation_accepted"), is(false));
          assertThat(edge.property("ckccrelation_accepted").isPresent(), is(false));
          break;
        case ckccOnlyId:
          assertThat(edge.value("ckccrelation_accepted"), is(true));
          assertThat(edge.property("wwrelation_accepted").isPresent(), is(false));
          break;
        default:
          throw new RuntimeException("A case was not explicitly handled");
      }
    });
  }

  @Test
  public void throwsNotFoundWhenTheEntityIsNotOfThisVre() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = testGraph.newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ckcc")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("deleted", false)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(NotFoundException.class);

    instance.delete("wwpersons", UUID.fromString(id), "");
  }

  @Test
  public void throwsNotFoundWhenTheIdIsNotInTheDatabase() throws Exception {
    String id = UUID.randomUUID().toString();
    String otherId = UUID.randomUUID().toString();
    Graph graph = testGraph.newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(NotFoundException.class);
    instance.delete("wwpersons", UUID.fromString(otherId), "");
  }

}
