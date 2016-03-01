package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.node.ArrayNode;
import nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

import java.net.URI;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class AutocompleteTest {

  public TinkerpopJsonCrudService basicInstance(Graph graph) {
    Vres map = new Vres.Builder()
      .withVre("WomenWriters", "ww", vre -> vre
        .withCollection("wwpersons", c -> c
          .withDisplayName(PropertyTypes.localProperty("displayName"))
        )
      )
      .build();
    UrlGenerator generator = (collection, id, rev) ->
      URI.create("http://example.com/" + collection + "/" + id + "?rev=" + rev);
    Clock clock = Clock.systemDefaultZone();
    HandleAdder handleAdder = mock(HandleAdder.class);

    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    when(graphWrapper.getGraph()).thenReturn(graph);

    return new TinkerpopJsonCrudService(graphWrapper, map, handleAdder, null, generator, clock);
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
    TinkerpopJsonCrudService instance = basicInstance(graph);

    ArrayNode result = instance.autoComplete("wwpersons", Optional.of("*author*"), Optional.empty());

    assertThat(result.toString(), sameJSONAs(jsnA(
      jsnO("key", jsn("An author"), "value", jsn("http://example.com/wwpersons/" + id + "?rev=2"))
    ).toString()));
  }
}
