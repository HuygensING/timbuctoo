package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.test.RegexMatcher;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class TinkerpopJsonCrudServiceReplaceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  public TinkerpopJsonCrudService basicInstance(Graph graph) {
    return customInstanceMaker(graph, null, null, null, null);
  }

  public TinkerpopJsonCrudService basicInstanceWithClock(Graph graph, Clock clock) {
    return customInstanceMaker(graph, null, null, clock, null);
  }

  public TinkerpopJsonCrudService basicInstanceWithVre(Graph graph, Vres vre) {
    return customInstanceMaker(graph, vre, null, null, null);
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
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(InvalidCollectionException.class);

    instance.replace("not_wwpersons", null, null, "");
  }

  @Test
  public void increasesRevisionByOne() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withIncomingRelation("VERSION_OF", "orig")
      )
      .withVertex("orig", v -> v
        .withTimId(id)
        .withProperty("isLatest", false)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.replace("wwpersons", UUID.fromString(id), jsnO("^rev", jsn(1)), "");

    int rev = (int) graph.traversal().V().has("tim_id", id).has("isLatest", true).properties("rev").value().next();

    assertThat(rev, is(2));
  }

  @Test
  public void addsType() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ckcc")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("ckccperson_name", "the name")
        .withIncomingRelation("VERSION_OF", "orig")
      )
      .withVertex("orig", v -> v
        .withTimId(id)
        .withVre("ckcc")
        .withType("person")
        .withProperty("isLatest", false)
        .withProperty("rev", 1)
        .withProperty("ckccperson_name", "the name")
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.replace("wwpersons", UUID.fromString(id), jsnO("^rev", jsn(1)), "");

    String types = (String) graph.traversal().V()
      .has("tim_id", id)
      .has("isLatest", true)
      .properties("types").value()
      .next();

    assertThat(types, containsString("\"wwperson\""));
  }

  @Test
  public void setsModified() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
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

    instance.replace("wwpersons", UUID.fromString(id), jsnO("^rev", jsn(1)), "despicable_me");
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
  public void throwsOnUnknownProperties() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(IOException.class);
    //message should contain the property that is unrecognized
    expectedException.expectMessage(new RegexMatcher(Pattern.compile(".*Unknownproperty.*")));

    instance.replace("wwpersons", UUID.fromString(id), jsnO("Unknownproperty", jsn(), "^rev", jsn(1)), "");
  }

  @Test
  public void setsJsonPropertyMapForKnownProperties() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("wwperson_name", "oldname")
        //property age is omitted
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstanceWithVre(
      graph,
      new Vres.Builder()
        .withVre("womenwriters", "ww", vre -> vre
          .withCollection("wwpersons", c -> c
            .withProperty("name", localProperty("wwperson_name"))
            .withProperty("age", localProperty("wwperson_age"))
          )
        )
        .build()
    );

    instance.replace("wwpersons", UUID.fromString(id), jsnO(
      "name", jsn("newName"),
      "age", jsn("42"),
      "^rev", jsn(1)
    ), "");

    Map<String, Object> result = graph.traversal().V()
      .has("tim_id", id)
      .has("isLatest", true)
      .as("vertex")
      .properties("wwperson_name").value().as("name").select("vertex")
      .properties("wwperson_age").value().as("age").select("vertex")
      .select("name", "age")
      .next();

    assertThat(result.get("name"), is("newName"));
    assertThat(result.get("age"), is("42"));
  }

  @Test
  public void removesPropertiesNoLongerProvided() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("wwperson_name", "oldname")
        //property age is omitted
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstanceWithVre(
      graph,
      new Vres.Builder()
        .withVre("womenwriters", "ww", vre -> vre
          .withCollection("wwpersons", c -> c
            .withProperty("name", localProperty("wwperson_name"))
            .withProperty("age", localProperty("wwperson_age"))
          )
        )
        .build()
    );

    instance.replace("wwpersons", UUID.fromString(id), jsnO(
      "age", jsn("42"),
      "^rev", jsn(1)
    ), "");

    Vertex result = graph.traversal().V()
      .has("tim_id", id)
      .has("isLatest", true)
      .next();

    assertThat(result.property("wwperson_name").isPresent(), is(false));
  }

  @Test
  public void preparesBackupCopyAfterMakingChanges() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withIncomingRelation("VERSION_OF", "orig")
      )
      .withVertex("orig", v -> v
        .withTimId(id)
        .withProperty("isLatest", false)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    Vertex beforeUpdate = graph.traversal().V()
      .has("tim_id", id)
      .has("isLatest", true)
      .next();

    instance.replace("wwpersons", UUID.fromString(id), jsnO("^rev", jsn(1)), "");

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
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();

    TinkerpopJsonCrudService instance = basicInstance(graph);
    instance.replace("wwpersons", UUID.fromString(id), jsnO("^rev", jsn(1)), "");

    graph.tx().close();

    assertThat(graph.traversal().V().toList().size(), is(2));
  }

  @Test
  public void throwsWhenPropertyMapperThrowsProperties() throws Exception {
    LocalProperty throwingMap = mock(LocalProperty.class);
    doThrow(new IOException("MOCKED PARSE ERROR")).when(throwingMap).setJson(any(), any());

    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstanceWithVre(
      graph,
      new Vres.Builder()
        .withVre("WomenWriters", "ww", vre -> vre
          .withCollection("wwpersons", c -> c
            .withProperty("name", throwingMap)
          )
        ).build()
    );
    expectedException.expect(IOException.class);
    //message should contain the property that is unrecognized
    expectedException.expectMessage(new RegexMatcher(Pattern.compile(".*name.*")));

    instance.replace("wwpersons", UUID.fromString(id), JsonBuilder.jsnO("name", jsn("Hans"), "^rev", jsn(1)), "");
  }

  @Test
  public void addsPersistentId() throws Exception {
    String uuid = UUID.randomUUID().toString();
    int oldRev = 3;
    Graph graph = newGraph()
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

    instance.replace("wwpersons", UUID.fromString(uuid), JsonBuilder.jsnO("^rev", jsn(oldRev)), "");

    verify(handleAdder, times(1)).add(
      new HandleAdderParameters(
        UUID.fromString(uuid),
        oldRev + 1,
        URI.create("http://example.com/" + uuid + "?r=" + (oldRev + 1))
      )
    );
  }

  @Test
  public void ignoresAtTypeAnd_id() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.replace("wwpersons", UUID.fromString(id), jsnO("@type", jsn(), "_id", jsn(), "^rev", jsn(1)), "");

    //Look mom! no exceptions!

    //let's check if this did anything at all:
    int rev = (int) graph.traversal().V().has("tim_id", id).has("isLatest", true).properties("rev").value().next();
    assertThat(rev, is(2));
  }

  @Test
  public void movesRelationsToNewestVertex() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
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

    instance.replace("wwpersons", UUID.fromString(id), jsnO("^rev", jsn(1)), "");

    Vertex replacement = graph.traversal().V().has("tim_id", id).has("isLatest", true).next();
    assertThat(stream(orig.edges(Direction.BOTH, "hasWritten", "isFriendOf")).count(), is(0L));
    assertThat(stream(replacement.edges(Direction.BOTH, "hasWritten", "isFriendOf")).count(), is(2L));
  }

  @Test
  public void throwsNotFoundWhenTheIdIsNotInTheDatabase() throws Exception {
    String id = UUID.randomUUID().toString();
    String otherId = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(NotFoundException.class);
    instance.replace("wwpersons", UUID.fromString(otherId), jsnO("^rev", jsn(1)), "");
  }

  @Test
  public void throwsAlreadyUpdatedIfTheRevDoesNotMatch() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(AlreadyUpdatedException.class);
    instance.replace("wwpersons", UUID.fromString(id), jsnO(
      "^rev", jsn(1)
    ), "");
  }

}
