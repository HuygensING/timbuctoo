package nl.knaw.huygens.timbuctoo.crud;

import com.google.common.collect.Iterators;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
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
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class TinkerpopJsonCrudServiceCreateTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  public TinkerpopJsonCrudService basicInstance(Graph graph) {
    return customInstanceMaker(graph, null, null, null, null);
  }

  public TinkerpopJsonCrudService basicInstanceWithClock(Graph graph, Clock clock) {
    return customInstanceMaker(graph, null, null, clock, null);
  }

  public TinkerpopJsonCrudService basicInstanceWithMap(Graph graph, Vres map) {
    return customInstanceMaker(graph, map, null, null, null);
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
  public void addsVertexToTheGraph() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();

    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.create("wwpersons", JsonBuilder.jsnO(), "");

    assertThat(graph.vertices().next(), is(not(nullValue())));
  }

  @Test
  public void throwsOnUnknownMappings() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    expectedException.expect(InvalidCollectionException.class);

    instance.create("not_wwpersons", JsonBuilder.jsnO(), "");
  }

  @Test
  public void setsTheTimIdPropertyAndReturnsIt() throws Exception {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    UUID id = instance.create("wwpersons", JsonBuilder.jsnO(), "");

    assertThat(graph.vertices().next().value("tim_id"), is(id.toString()));
  }

  @Test
  public void setsRevisionToOne() throws IOException, InvalidCollectionException {
    //because a newly created item is always revision 1
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.create("wwpersons", JsonBuilder.jsnO(), "");

    assertThat(graph.vertices().next().value("rev"), is(1));
  }

  @Test
  public void setsTypeToCollectionAndBaseCollection() throws IOException, InvalidCollectionException {
    //a wwperson is also a person
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.create("wwpersons", JsonBuilder.jsnO(), "");

    assertThat(graph.vertices().next().value("types"), is("[\"wwperson\", \"person\"]"));
  }

  @Test
  public void setsCreated() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    int oneSecondPast1970 = 1000;
    TinkerpopJsonCrudService instance = basicInstanceWithClock(
      graph,
      Clock.fixed(Instant.ofEpochMilli(oneSecondPast1970), ZoneId.systemDefault())
    );

    instance.create("wwpersons", JsonBuilder.jsnO(), "despicable_me");

    assertThat(
      graph.vertices().next().value("created"),
      sameJSONAs(String.format("{\"timeStamp\": %s,\"userId\": \"%s\"}", oneSecondPast1970, "despicable_me"))
    );
  }

  @Test
  public void setsCreatedAndModifiedToTheSameValue() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

    instance.create("wwpersons", JsonBuilder.jsnO(), "");

    assertThat(graph.vertices().next().value("created"), is(graph.vertices().next().value("modified").toString()));
  }

  @Test
  public void throwsOnUnknownProperties() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = basicInstanceWithMap(
      graph,
      new Vres.Builder()
        .withVre("WomenWriters", "ww", vre -> vre
          .withCollection("wwpersons", c -> c
            .withProperty("name", localProperty("wwname"))
          )
        ).build()
    );

    expectedException.expect(IOException.class);
    //message should contain the property that is unrecognized
    expectedException.expectMessage(new RegexMatcher(Pattern.compile(".*birthplace.*")));

    instance.create("wwpersons", JsonBuilder.jsnO("birthplace", jsn("Moordrecht")), "");
  }

  @Test
  public void setsJsonPropertyMapForKnownProperties() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = basicInstanceWithMap(
      graph,
      new Vres.Builder()
        .withVre("WomenWriters", "ww", vre -> vre
          .withCollection("wwpersons", c -> c
            .withProperty("name", localProperty("wwname"))
            .withProperty("age", localProperty("wwage"))
          )
        ).build()
    );

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
  public void throwsWhenPropertyMapperThrowsProperties() throws IOException, InvalidCollectionException {
    LocalProperty throwingMap = mock(LocalProperty.class);
    doThrow(new IOException("PARSE ERROR")).when(throwingMap).setJson(any(), any());

    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = basicInstanceWithMap(
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
  public void ignoresAtTypeProperty() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = basicInstance(graph);

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
    TinkerpopJsonCrudService instance = basicInstance(graph);

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
    TinkerpopJsonCrudService instance = basicInstance(graph);

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
    TinkerpopJsonCrudService instance = basicInstanceWithUrlGenerator(
      graph,
      (collectionName, id, rev) -> URI.create("http://example.com?id=" + id + "&rev=" + rev),
      handleAdder
    );

    UUID uuid = instance.create("wwpersons", JsonBuilder.jsnO(), "");

    verify(handleAdder, times(1)).add(
      new HandleAdderParameters(uuid, 1, URI.create("http://example.com?id=" + uuid + "&rev=1"))
    );
  }

}
