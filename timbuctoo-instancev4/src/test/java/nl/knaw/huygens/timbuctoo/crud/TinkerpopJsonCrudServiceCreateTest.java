package nl.knaw.huygens.timbuctoo.crud;

import com.google.common.collect.Iterators;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.test.RegexMatcher;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import java.util.regex.Pattern;

import static nl.knaw.huygens.timbuctoo.util.AuthorizerHelper.userIsNotAllowedToWriteTheCollection;
import static nl.knaw.huygens.timbuctoo.crud.JsonCrudServiceBuilder.newJsonCrudService;
import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class TinkerpopJsonCrudServiceCreateTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void addsVertexToTheGraph() throws Exception {
    Graph graph = newGraph().build();

    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.create("wwpersons", JsonBuilder.jsnO(), "");

    assertThat(graph.vertices().next(), is(not(nullValue())));
  }

  @Test
  public void throwsOnUnknownMappings() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    expectedException.expect(InvalidCollectionException.class);

    instance.create("not_wwpersons", JsonBuilder.jsnO(), "");
  }

  @Test
  public void setsTheTimIdPropertyAndReturnsIt() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    UUID id = instance.create("wwpersons", JsonBuilder.jsnO(), "");

    assertThat(graph.vertices().next().value("tim_id"), is(id.toString()));
  }

  @Test
  public void setsRevisionToOne() throws Exception {
    //because a newly created item is always revision 1
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.create("wwpersons", JsonBuilder.jsnO(), "");

    assertThat(graph.vertices().next().value("rev"), is(1));
  }

  @Test
  public void setsTypeToCollectionAndBaseCollection() throws Exception {
    //a wwperson is also a person
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.create("wwpersons", JsonBuilder.jsnO(), "");

    assertThat(graph.vertices().next().value("types"), is("[\"wwperson\", \"person\"]"));
  }

  @Test
  public void setsCreated() throws Exception {
    Graph graph = newGraph().build();
    int oneSecondPast1970 = 1000;
    TinkerpopJsonCrudService instance =
      newJsonCrudService().withClock(Clock.fixed(Instant.ofEpochMilli(oneSecondPast1970), ZoneId.systemDefault()))
                          .forGraph(graph);

    instance.create("wwpersons", JsonBuilder.jsnO(), "despicable_me");

    assertThat(
      graph.vertices().next().value("created"),
      sameJSONAs(String.format("{\"timeStamp\": %s,\"userId\": \"%s\"}", oneSecondPast1970, "despicable_me"))
    );
  }

  @Test
  public void setsCreatedAndModifiedToTheSameValue() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.create("wwpersons", JsonBuilder.jsnO(), "");

    assertThat(graph.vertices().next().value("created"), is(graph.vertices().next().value("modified").toString()));
  }

  @Test
  public void throwsOnUnknownProperties() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().withVres(new Vres.Builder()
      .withVre("WomenWriters", "ww", vre -> vre
        .withCollection("wwpersons", c -> c
          .withProperty("name", localProperty("wwname"))
        )
      ).build()).forGraph(graph);

    expectedException.expect(IOException.class);
    //message should contain the property that is unrecognized
    expectedException.expectMessage(new RegexMatcher(Pattern.compile(".*birthplace.*")));

    instance.create("wwpersons", JsonBuilder.jsnO("birthplace", jsn("Moordrecht")), "");
  }

  @Test
  public void setsJsonPropertyMapForKnownProperties() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().withVres(new Vres.Builder()
      .withVre("WomenWriters", "ww", vre -> vre
        .withCollection("wwpersons", c -> c
          .withProperty("name", localProperty("wwname"))
          .withProperty("age", localProperty("wwage"))
        )
      ).build()).forGraph(graph);

    instance.create(
      "wwpersons",
      JsonBuilder.jsnO(
        "name", jsn("Hans"),
        "age", jsn("12")
      ),
      ""
    );
    assertThat(graph.vertices().next().value("wwname"), is("Hans"));
    assertThat(graph.vertices().next().value("wwage"), is("12"));
  }

  @Test
  public void throwsWhenPropertyMapperThrowsProperties() throws Exception {
    LocalProperty throwingMap = mock(LocalProperty.class);
    doThrow(new IOException("PARSE ERROR")).when(throwingMap).setJson(any(), any());

    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().withVres(new Vres.Builder()
      .withVre("WomenWriters", "ww", vre -> vre
        .withCollection("wwpersons", c -> c
          .withProperty("name", throwingMap)
        )
      ).build()).forGraph(graph);
    expectedException.expect(IOException.class);
    //message should contain the property that is unrecognized
    expectedException.expectMessage(new RegexMatcher(Pattern.compile(".*name.*")));

    instance.create(
      "wwpersons",
      JsonBuilder.jsnO(
        "name", jsn("Hans")
      ),
      ""
    );
  }

  /*
   * @type is allowed by the previous implementation, but is always identical to the collectionName
   * We've chosen to not throw a 400 when the collection and the type mismatch
   */
  @Test
  public void ignoresAtTypeProperty() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    assertThat(graph.vertices().hasNext(), is(false));
    instance.create(
      "wwpersons",
      JsonBuilder.jsnO(
        "@type", jsn("foo")
      ),
      ""
    );
    assertThat(graph.vertices().hasNext(), is(true));
  }

  @Test
  public void commitsChangesIfEverythingSucceeds() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    assertThat(graph.vertices().hasNext(), is(false));
    instance.create(
      "wwpersons",
      JsonBuilder.jsnO(),
      ""
    );
    graph.tx().close();
    assertThat(graph.vertices().hasNext(), is(true));
  }

  @Test
  public void preparesBackupCopyAfterMakingChanges() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    assertThat(graph.vertices().hasNext(), is(false));
    instance.create(
      "wwpersons",
      JsonBuilder.jsnO(),
      ""
    );

    //graph contains two nodes with a
    Vertex copy = graph.traversal().V().has("isLatest", true).next();
    Vertex original = graph.traversal().V().has("isLatest", false).next();

    assertThat(Iterators.size(copy.edges(Direction.IN, "VERSION_OF")), is(1));
    assertThat(copy.edges(Direction.IN, "VERSION_OF").next().outVertex().id(), is(original.id()));
    assertThat(Iterators.size(graph.vertices()), is(2));
    assertThat(copy.value("rev"), is((Integer) original.value("rev")));
  }

  @Test
  public void addsPersistentId() throws Exception {
    Graph graph = newGraph().build();
    HandleAdder handleAdder = mock(HandleAdder.class);
    UrlGenerator urlGen = (collectionName, id, rev) -> URI.create("http://example.com?id=" + id + "&rev=" + rev);
    TinkerpopJsonCrudService instance =
      newJsonCrudService().withUrlGenerator(urlGen).withHandleAdder(handleAdder).forGraph(graph);

    UUID uuid = instance.create("wwpersons", JsonBuilder.jsnO(), "");

    verify(handleAdder, times(1)).add(
      new HandleAdderParameters(uuid, 1, URI.create("http://example.com?id=" + uuid + "&rev=1"))
    );
  }

  // Security tests
  @Test
  public void throwsAnAuthorizationExceptionWhenTheUserIsNotAllowedToAlterTheCollection() throws Exception {
    Graph graph = newGraph().build();
    String collectionName = "wwpersons";
    String userId = "userId";
    Authorizer authorizer = userIsNotAllowedToWriteTheCollection(collectionName, userId);
    TinkerpopJsonCrudService instance = newJsonCrudService().withAuthorizer(authorizer).forGraph(graph);

    expectedException.expect(AuthorizationException.class);

    instance.create(collectionName, JsonBuilder.jsnO(), userId);
  }

}
