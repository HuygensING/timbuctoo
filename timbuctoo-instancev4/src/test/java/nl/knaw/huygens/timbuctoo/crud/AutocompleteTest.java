package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.node.ArrayNode;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.TestGraphBuilder;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.crud.JsonCrudServiceBuilder.newJsonCrudService;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class AutocompleteTest {

  public JsonCrudServiceBuilder basicInstance(Graph graph) {
    JsonCrudServiceBuilder builder = newJsonCrudService();
    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    when(graphWrapper.getCurrentEntitiesFor(anyString())).then(x -> graph.traversal().V());

    return builder.withGraphWrapper(graphWrapper);
  }


  @Test
  public void works() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
        .withProperty("displayName", "An author")
      )
      .withVertex("orig", v -> v
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 1)
        .withProperty("displayName", "Une Auteur")
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph)
      .withAutocompletenUrlGenerator(
        (collection, id1, rev) -> URI.create("http://example.com/" + collection + "/" + id1 + "?rev=" + rev)
      )
      .build();

    ArrayNode result = instance.autoComplete("wwpersons", Optional.of("*author*"), Optional.empty());

    assertThat(result.toString(), sameJSONAs(jsnA(
      jsnO("value", jsn("An author"), "key", jsn("http://example.com/wwpersons/" + id + "?rev=2"))
    ).toString()));
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void throwsUnknownCollection() throws Exception {
    Graph graph = newGraph()
      .build();
    TinkerpopJsonCrudService instance = newJsonCrudService().forGraph(graph);

    expectedException.expect(InvalidCollectionException.class);
    instance.autoComplete("AnUnknownCollection", Optional.empty(), Optional.empty());
  }

  @Test
  public void usesGetCurrentEntitiesFor() throws Exception {
    JsonCrudServiceBuilder builder = basicInstance(newGraph().build());
    TinkerpopJsonCrudService instance = builder.build();

    instance.autoComplete("wwpersons", Optional.of("author"), Optional.empty());

    verify(builder.getGraphWrapperMock()).getCurrentEntitiesFor("wwperson");
  }

  @Test
  public void ignoresPreOrPostfixStars() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
        .withProperty("displayName", "An author")
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph).build();

    ArrayNode result = instance.autoComplete("wwpersons", Optional.of("author"), Optional.empty());
    ArrayNode resultprefix = instance.autoComplete("wwpersons", Optional.of("*author"), Optional.empty());
    ArrayNode resultpostfix = instance.autoComplete("wwpersons", Optional.of("author*"), Optional.empty());
    ArrayNode resultwrapped = instance.autoComplete("wwpersons", Optional.of("*author*"), Optional.empty());

    assertThat(result, is(resultprefix));
    assertThat(result, is(resultpostfix));
    assertThat(result, is(resultwrapped));
    assertThat(result.size(), is(1));
  }

  @Test
  public void showsDisplayNames() throws Exception {
    String id = UUID.randomUUID().toString();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id)
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
        .withProperty("displayName", "An author")
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph).build();

    ArrayNode result = instance.autoComplete("wwpersons", Optional.of("author"), Optional.empty());
    assertThat(result.get(0).get("value").asText(), is("An author"));
  }

  @Test
  public void usesAutocompleteUrlGeneratorToGenerateTheUrl() throws Exception {
    UUID id = UUID.randomUUID();
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(id.toString())
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
        .withProperty("displayName", "An author")
      )
      .build();
    UrlGenerator gen = mock(UrlGenerator.class);
    when(gen.apply("wwpersons", id, 2)).thenReturn(URI.create("http://example.com/URI"));

    TinkerpopJsonCrudService instance = basicInstance(graph)
      .withAutocompletenUrlGenerator(gen)
      .build();

    instance.autoComplete("wwpersons", Optional.of("author"), Optional.empty());
    verify(gen).apply("wwpersons", id, 2);
  }

  @Test
  public void givesFiftyResultsWhenFiltering() throws Exception {
    TestGraphBuilder builder = newGraph();
    for (int i = 0; i < 51; i++) {
      builder.withVertex(v -> v
        .withTimId(UUID.randomUUID().toString())
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("displayName", "An author")
      );
    }
    TinkerpopJsonCrudService instance = basicInstance(builder.build()).build();

    ArrayNode result = instance.autoComplete("wwpersons", Optional.of("author"), Optional.empty());
    assertThat(result.size(), is(50));
  }

  @Test
  public void givesThousandResultsWhenNotFiltering() throws Exception {
    TestGraphBuilder builder = newGraph();
    for (int i = 0; i < 1001; i++) {
      builder.withVertex(v -> v
        .withTimId(UUID.randomUUID().toString())
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("displayName", "An author")
      );
    }
    TinkerpopJsonCrudService instance = basicInstance(builder.build()).build();

    ArrayNode result = instance.autoComplete("wwpersons", Optional.empty(), Optional.empty());
    assertThat(result.size(), is(1000));
  }

  @Test
  public void givesSortedResults() throws Exception {
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(UUID.randomUUID().toString())
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
        .withProperty("displayName", "A author")
      )
      .withVertex(v -> v
        .withTimId(UUID.randomUUID().toString())
        .withVre("ww")
        .withType("person")
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
        .withProperty("displayName", "Z author")
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph)
      .build();

    ArrayNode jsonNodes = instance.autoComplete("wwpersons", Optional.of("author"), Optional.empty());
    assertThat(jsonNodes.get(0).get("value").asText(), is("A author"));
    assertThat(jsonNodes.get(1).get("value").asText(), is("Z author"));
  }

  @Test
  public void filtersOnKeywords() throws Exception {
    Graph graph = newGraph()
      .withVertex(v -> v
        .withTimId(UUID.randomUUID().toString())
        .withVre("ww")
        .withType("keyword")
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
        .withProperty("keyword_type", "language")
        .withProperty("displayName", "a keyword")
      )
      .withVertex(v -> v
        .withTimId(UUID.randomUUID().toString())
        .withVre("ww")
        .withType("keyword")
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
        .withProperty("keyword_type", "language")
        .withProperty("displayName", "another sleutelwoord")
      )
      .withVertex(v -> v
        .withTimId(UUID.randomUUID().toString())
        .withVre("ww")
        .withType("keyword")
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
        .withProperty("keyword_type", "country")
        .withProperty("displayName", "a keyword")
      )
      .build();
    TinkerpopJsonCrudService instance = basicInstance(graph)
      .build();

    ArrayNode jsonNodes = instance.autoComplete("wwkeywords", Optional.of("keyword"), Optional.of("language"));
    assertThat(jsonNodes.get(0).get("value").asText(), is("a keyword"));
    assertThat(jsonNodes.size(), is(1));
  }


}
