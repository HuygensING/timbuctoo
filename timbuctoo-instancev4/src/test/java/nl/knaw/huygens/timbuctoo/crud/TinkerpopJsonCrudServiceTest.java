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
import nl.knaw.huygens.timbuctoo.search.TestGraphBuilder;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.commons.lang.NotImplementedException;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.test.RegexMatcher;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static nl.knaw.huygens.timbuctoo.crud.TinkerpopJsonCrudServiceTest.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.search.TestGraphBuilder.newGraph;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class TinkerpopJsonCrudServiceTest {

  public Map<String, List<JsonToTinkerpopPropertyMap>> ONLY_WWPERSONS = ImmutableMap.of(
    "wwpersons", Lists.newArrayList()
  );

  public GraphWrapper asWrapper(Graph g) {
    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    when(graphWrapper.getGraph()).thenReturn(g);
    return graphWrapper;
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void addsVertexToTheGraph() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();

    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS);

    instance.create("wwpersons", jsn(), "");

    assertThat(graph.vertices().next(), is(not(nullValue())));
  }

  @Test
  public void throwsOnUnknownMappings() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS);

    expectedException.expect(InvalidCollectionException.class);

    instance.create("not_wwpersons", jsn(), "");
  }

  @Test
  public void setsTheTimIdPropertyAndReturnsIt() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS);

    UUID id = instance.create("wwpersons", jsn(), "");

    assertThat(graph.vertices().next().value("tim_id"), is(id.toString()));
  }

  @Test
  public void setsRevisionToOne() throws IOException, InvalidCollectionException {
    //because a newly created item is always revision 1
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS);

    instance.create("wwpersons", jsn(), "");

    assertThat(graph.vertices().next().value("rev"), is(1));
  }

  @Test
  public void setsTypeToCollectionAndBaseCollection() throws IOException, InvalidCollectionException {
    //a wwperson is also a person
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS);

    instance.create("wwpersons", jsn(), "");

    assertThat(graph.vertices().next().value("types"), is("[\"wwperson\", \"person\"]"));
  }

  @Test
  public void setsCreated() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    int ONE_SECOND_PAST_1970 = 1000;
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS, Clock.fixed(Instant.ofEpochMilli(ONE_SECOND_PAST_1970), ZoneId.systemDefault()));
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(
      asWrapper(graph),
      ONLY_WWPERSONS,
      Clock.fixed(Instant.ofEpochMilli(ONE_SECOND_PAST_1970), ZoneId.systemDefault())
    );

    instance.create("wwpersons", jsn(), "despicable_me");

    assertThat(graph.vertices().next().value("created"), sameJSONAs(String.format("{\"timeStamp\": %s,\"userId\": \"%s\"}", ONE_SECOND_PAST_1970, "despicable_me")));
    assertThat(
      graph.vertices().next().value("created"),
      sameJSONAs(String.format("{\"timeStamp\": %s,\"userId\": \"%s\"}", ONE_SECOND_PAST_1970, "despicable_me"))
    );
  }

  @Test
  public void setsCreatedAndModifiedToTheSameValue() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS);

    instance.create("wwpersons", jsn(), "");

    assertThat(graph.vertices().next().value("created"), is(graph.vertices().next().value("modified").toString()));
  }

  @Test
  public void throwsOnUnknownProperties() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(
      asWrapper(graph),
      ImmutableMap.of("wwpersons", Lists.newArrayList(new EncodeJsonMap("name", "wwname")))
    );

    expectedException.expect(IOException.class);
    //message should contain the property that is unrecognized
    expectedException.expectMessage(new RegexMatcher(Pattern.compile(".*birthplace.*")));

    instance.create("wwpersons", jsn("birthplace", jsn("Moordrecht")), "");
  }

  @Test
  public void setsJsonPropertyMapForKnownProperties() throws IOException, InvalidCollectionException {
    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(
      asWrapper(graph),
      ImmutableMap.of("wwpersons", Lists.newArrayList(
        new EncodeJsonMap("name", "wwname"),
        new EncodeJsonMap("age", "wwage")
      ))
    );

    instance.create(
      "wwpersons",
      jsn(
        "name", jsn("Hans"),
        "age", jsn(12)
      ),
      ""
    );
    assertThat(graph.vertices().next().value("wwname"), is("\"Hans\""));
    assertThat(graph.vertices().next().value("wwage"), is("12"));
  }

  @Test
  public void throwsWhenPropertyMapperThrowsProperties() throws IOException, InvalidCollectionException {
    JsonToTinkerpopPropertyMap throwingMap = mock(JsonToTinkerpopPropertyMap.class);
    when(throwingMap.jsonToTinkerpop(any())).then(x -> { throw new IOException("PARSE ERROR"); });
    when(throwingMap.getJsonName()).thenReturn("name");
    when(throwingMap.getTinkerpopName()).thenReturn("wwname");

    Graph graph = newGraph().build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(
      asWrapper(graph),
      ImmutableMap.of("wwpersons", Lists.newArrayList(
        throwingMap
      ))
    );
    expectedException.expect(IOException.class);
    //message should contain the property that is unrecognized
    expectedException.expectMessage(new RegexMatcher(Pattern.compile(".*name.*")));

    instance.create(
      "wwpersons",
      jsn(
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
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS);

    assertThat(graph.vertices().hasNext(), is(false));
    instance.create(
      "wwpersons",
      jsn(
        "@type", jsn("foo")
      ),
      ""
    );
    assertThat(graph.vertices().hasNext(), is(true));
  }

  @Test
  public void commitsChangesIfEverythingSucceeds() throws Exception {
    TestGraphBuilder graphBuilder = newGraph();
    Graph graph = graphBuilder.build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS);

    assertThat(graph.vertices().hasNext(), is(false));
    instance.create(
      "wwpersons",
      jsn(),
      ""
    );
    graph.tx().close();
    assertThat(graph.vertices().hasNext(), is(true));
  }

  @Test
  public void preparesBackupCopyAfterMakingChanges() throws Exception {
    TestGraphBuilder graphBuilder = newGraph();
    Graph graph = graphBuilder.build();
    TinkerpopJsonCrudService instance = new TinkerpopJsonCrudService(asWrapper(graph), ONLY_WWPERSONS);

    assertThat(graph.vertices().hasNext(), is(false));
    instance.create(
      "wwpersons",
      jsn(),
      ""
    );

    //graph contains two nodes with a
    Vertex copy = graph.traversal().V().has("isLatest", true).next();
    Vertex original = graph.traversal().V().has("isLatest", false).next();

    assertThat(Iterators.size(copy.edges(Direction.IN, "VERSION_OF")), is(1));
    assertThat(copy.edges(Direction.IN, "VERSION_OF").next().outVertex().id(), is(original.id()));
    assertThat(Iterators.size(graph.vertices()), is(2));
  }

  @Test
  @Ignore
  public void addsPersistentId() {
    throw new NotImplementedException();
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
