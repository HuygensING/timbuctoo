package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.crud.JsonCrudServiceBuilder.newJsonCrudService;
import static nl.knaw.huygens.timbuctoo.util.AuthorizerHelper.authorizerThrowsAuthorizationUnavailableException;
import static nl.knaw.huygens.timbuctoo.util.AuthorizerHelper.userIsNotAllowedToWriteTheCollection;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class TinkerpopJsonCrudServiceDeleteTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void throwsOnUnknownMappings() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    expectedException.expect(InvalidCollectionException.class);

    instance.delete("not_wwpersons", null, "");
  }

  @Test
  public void increasesRevisionByOne() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
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
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.delete("wwpersons", UUID.fromString(id), "");

    int rev = (int) graph.traversal().V().has("tim_id", id).has("isLatest", true).properties("rev").value().next();

    assertThat(rev, is(2));
  }

  @Test
  public void removesTypeWhenOtherTypesExist() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ckcc")
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.delete("wwpersons", UUID.fromString(id), "");

    String types = (String) graph.traversal().V()
                                 .has("tim_id", id)
                                 .has("isLatest", true)
                                 .properties("types").value()
                                 .next();

    assertThat(types, is("[\"ckccperson\"]"));
  }

  @Test
  public void setsDeletedToTrueWhenLastTypeIsRemoved() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("deleted", false)
      )
      .build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

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
    TinkerpopJsonCrudService instance =
      newJsonCrudService().withClock(Clock.fixed(Instant.ofEpochMilli(oneSecondPast1970), ZoneId.systemDefault()))
                          .forGraph(graph);

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
    Graph graph = newGraph()
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
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

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
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();

    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);
    instance.delete("wwpersons", UUID.fromString(id), "");

    graph.tx().close();

    assertThat(graph.traversal().V().toList().size(), is(2));
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
    UrlGenerator urlGen = (collectionName, id, rev) -> URI.create("http://example.com/" + id + "?r=" + rev);
    TinkerpopJsonCrudService instance =
      newJsonCrudService().withUrlGenerator(urlGen).withHandleAdder(handleAdder).forGraph(graph);

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
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

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
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withOutgoingRelation("hasWritten", "document", rel -> rel
          .withTim_id(UUID.fromString(wwOnlyId))
          .removeType("ckcc")
          .withAccepted("wwrelation", true)
        )
        .withIncomingRelation("isFriendOf", "friend", rel -> rel
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
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.delete("wwpersons", UUID.fromString(id), "");

    Vertex replacement = graph.traversal().V().has("tim_id", id).has("isLatest", true).next();

    assertThat(stream(replacement.edges(Direction.BOTH, "hasWritten", "isFriendOf")).count(), is(3L));
    replacement.edges(Direction.BOTH, "hasWritten", "isFriendOf").forEachRemaining(edge -> {
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
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ckcc")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("deleted", false)
      )
      .build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    expectedException.expect(NotFoundException.class);

    instance.delete("wwpersons", UUID.fromString(id), "");
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
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    expectedException.expect(NotFoundException.class);
    instance.delete("wwpersons", UUID.fromString(otherId), "");
  }

  // Security tests
  @Test
  public void throwsAnAuthorizationExceptionWhenTheUserIsNotAllowedToAlterTheCollection() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    String collectionName = "wwpersons";
    String userId = "userId";
    Authorizer authorizer = userIsNotAllowedToWriteTheCollection(collectionName, userId);
    TinkerpopJsonCrudService instance = newJsonCrudService().withAuthorizer(authorizer).forGraph(graph);

    expectedException.expect(AuthorizationException.class);

    instance.delete(collectionName, id, userId);
  }

  @Test
  public void throwsAnIoExceptionWhenTheAuthorizerThrowsAnAuthorizationUnavailableException() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
      )
      .build();
    String collectionName = "wwpersons";
    String userId = "userId";
    Authorizer authorizer = authorizerThrowsAuthorizationUnavailableException();
    TinkerpopJsonCrudService instance = newJsonCrudService().withAuthorizer(authorizer).forGraph(graph);

    expectedException.expect(IOException.class);

    instance.delete(collectionName, id, userId);
  }

}
