package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.JsonToTinkerpopPropertyMap;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil;
import nl.knaw.huygens.timbuctoo.util.TestGraphBuilder;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.test.RegexMatcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudServiceCreateTest.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class TinkerpopJsonCrudServiceCreateTest {
  private final BiFunction<UUID, Integer, URI> dummyPidGenerator = (id, rev) -> {
    try {
      return new URI("http://example.com/");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  };

  private static final Map<String, List<JsonToTinkerpopPropertyMap>> ONLY_WWPERSONS = ImmutableMap.of(
    "wwpersons", Lists.newArrayList()
  );

  private HandleAdder handleAdder;

  @Before
  public void mockHandleAdder() {
    handleAdder = mock(HandleAdder.class);
  }

  public GraphWrapper asWrapper(Graph graph) {
    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    when(graphWrapper.getGraph()).thenReturn(graph);
    return graphWrapper;
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void addsVertexToTheGraph() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();

    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS, handleAdder);

    instance.create("wwpersons", jsn(), "", dummyPidGenerator);

    assertThat(graph.vertices().next(), is(not(nullValue())));
  }

  @Test
  public void throwsOnUnknownMappings() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS, handleAdder);

    expectedException.expect(InvalidCollectionException.class);

    instance.create("not_wwpersons", jsn(), "", dummyPidGenerator);
  }

  @Test
  public void setsTheTimIdPropertyAndReturnsIt() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS, handleAdder);

    UUID id = instance.create("wwpersons", jsn(), "", dummyPidGenerator);

    assertThat(graph.vertices().next().value("tim_id"), is(id.toString()));
  }

  @Test
  public void setsRevisionToOne() throws IOException, InvalidCollectionException {
    //because a newly created item is always revision 1
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS, handleAdder);

    instance.create("wwpersons", jsn(), "", dummyPidGenerator);

    assertThat(graph.vertices().next().value("rev"), is(1));
  }

  @Test
  public void setsTypeToCollectionAndBaseCollection() throws IOException, InvalidCollectionException {
    //a wwperson is also a person
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS, handleAdder);

    instance.create("wwpersons", jsn(), "", dummyPidGenerator);

    assertThat(graph.vertices().next().value("types"), is("[\"wwperson\", \"person\"]"));
  }

  @Test
  public void setsCreated() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    int oneSecondPast1970 = 1000;
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(
      asWrapper(graph),
      ONLY_WWPERSONS,
      handleAdder,
      Clock.fixed(Instant.ofEpochMilli(oneSecondPast1970), ZoneId.systemDefault())
    );

    instance.create("wwpersons", jsn(), "despicable_me", dummyPidGenerator);

    assertThat(
      graph.vertices().next().value("created"),
      sameJSONAs(String.format("{\"timeStamp\": %s,\"userId\": \"%s\"}", oneSecondPast1970, "despicable_me"))
    );
  }

  @Test
  public void setsCreatedAndModifiedToTheSameValue() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS, handleAdder);

    instance.create("wwpersons", jsn(), "", dummyPidGenerator);

    assertThat(graph.vertices().next().value("created"), is(graph.vertices().next().value("modified").toString()));
  }

  @Test
  public void throwsOnUnknownProperties() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(
      asWrapper(graph),
      ImmutableMap.of("wwpersons", Lists.newArrayList(new EncodeJsonMap("name", "wwname"))),
      handleAdder
    );

    expectedException.expect(IOException.class);
    //message should contain the property that is unrecognized
    expectedException.expectMessage(new RegexMatcher(Pattern.compile(".*birthplace.*")));

    instance.create("wwpersons", jsn("birthplace", jsn("Moordrecht")), "", dummyPidGenerator);
  }

  @Test
  public void setsJsonPropertyMapForKnownProperties() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(
      asWrapper(graph),
      ImmutableMap.of("wwpersons", Lists.newArrayList(
        new EncodeJsonMap("name", "wwname"),
        new EncodeJsonMap("age", "wwage")
      )),
      handleAdder
    );

    instance.create(
      "wwpersons",
      jsn(
        "name", jsn("Hans"),
        "age", jsn(12)
      ),
      "",
      dummyPidGenerator
    );
    assertThat(graph.vertices().next().value("wwname"), is("\"Hans\""));
    assertThat(graph.vertices().next().value("wwage"), is("12"));
  }

  @Test
  public void throwsWhenPropertyMapperThrowsProperties() throws IOException, InvalidCollectionException {
    JsonToTinkerpopPropertyMap throwingMap = mock(JsonToTinkerpopPropertyMap.class);
    when(throwingMap.jsonToTinkerpop(any())).then(x -> {
      throw new IOException("PARSE ERROR");
    });
    when(throwingMap.getJsonName()).thenReturn("name");
    when(throwingMap.getTinkerpopName()).thenReturn("wwname");

    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(
      asWrapper(graph),
      ImmutableMap.of("wwpersons", Lists.newArrayList(
        throwingMap
      )),
      handleAdder
    );
    expectedException.expect(IOException.class);
    //message should contain the property that is unrecognized
    expectedException.expectMessage(new RegexMatcher(Pattern.compile(".*name.*")));

    instance.create(
      "wwpersons",
      jsn(
        "name", jsn("Hans")
      ),
      "",
      dummyPidGenerator
    );
  }

  /*
   * @type is allowed by the previous implementation, but is always identical to the collectionName
   * We've chosen to not throw a 400 when the collection and the type mismatch
   */
  @Test
  public void ignoresAtTypeProperty() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS, handleAdder);

    assertThat(graph.vertices().hasNext(), is(false));
    instance.create(
      "wwpersons",
      jsn(
        "@type", jsn("foo")
      ),
      "",
      dummyPidGenerator
    );
    assertThat(graph.vertices().hasNext(), is(true));
  }

  @Test
  public void commitsChangesIfEverythingSucceeds() throws Exception {
    TestGraphBuilder graphBuilder = newGraph();
    Graph graph = graphBuilder.build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS, handleAdder);

    assertThat(graph.vertices().hasNext(), is(false));
    instance.create(
      "wwpersons",
      jsn(),
      "",
      dummyPidGenerator
    );
    graph.tx().close();
    assertThat(graph.vertices().hasNext(), is(true));
  }

  @Test
  public void preparesBackupCopyAfterMakingChanges() throws Exception {
    TestGraphBuilder graphBuilder = newGraph();
    Graph graph = graphBuilder.build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS, handleAdder);

    assertThat(graph.vertices().hasNext(), is(false));
    instance.create(
      "wwpersons",
      jsn(),
      "",
      dummyPidGenerator
    );

    //graph contains two nodes with a
    Vertex copy = graph.traversal().V().has("isLatest", true).next();
    Vertex original = graph.traversal().V().has("isLatest", false).next();

    assertThat(Iterators.size(copy.edges(Direction.IN, "VERSION_OF")), is(1));
    assertThat(copy.edges(Direction.IN, "VERSION_OF").next().outVertex().id(), is(original.id()));
    assertThat(Iterators.size(graph.vertices()), is(2));
  }

  @Test
  public void addsPersistentId() throws Exception {
    TestGraphBuilder graphBuilder = newGraph();
    Graph graph = graphBuilder.build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS, handleAdder);

    instance.create(
      "wwpersons",
      jsn(),
      "",
      LambdaExceptionUtil.rethrowBiFunction((id, rev) -> new URI("http://example.com?id=" + id + "&rev=" + rev))
    );

    Vertex orig = graph.traversal().V().has("isLatest", false).next();
    String uuid = orig.value("tim_id");
    Object vertexId = orig.id();

    verify(handleAdder, times(1)).add(
      new HandleAdderParameters(vertexId, new URI("http://example.com?id=" + uuid + "&rev=1"))
    );
  }

  public class EncodeJsonMap extends JsonToTinkerpopPropertyMap {
    public EncodeJsonMap(String jsonName, String tinkerpopName) {
      super(jsonName, tinkerpopName);
    }

    @Override
    public Object jsonToTinkerpop(JsonNode json) throws IOException {
      return json.toString();
    }
  }

  public static class JsonBuilder {
    public static JsonNodeFactory factory = JsonNodeFactory.instance;

    public static ObjectNode jsn(String prop1, JsonNode contents1) {
      ObjectNode result = factory.objectNode();
      result.set(prop1, contents1);
      return result;
    }

    public static ObjectNode jsn(String prop1, JsonNode contents1, String prop2, JsonNode contents2) {
      ObjectNode result = factory.objectNode();
      result.set(prop1, contents1);
      result.set(prop2, contents2);
      return result;
    }

    public static TextNode jsn(String val) {
      return factory.textNode(val);
    }

    public static NumericNode jsn(int val) {
      return factory.numberNode(val);
    }

    public static ObjectNode jsn() {
      return factory.objectNode();
    }
  }
}
