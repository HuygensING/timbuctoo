package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.JsonBasedUserStore;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.net.URI;
import java.time.Clock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonCrudServiceBuilder {
  private Vres vres;
  private Clock clock;
  private HandleAdder handleAdder;
  private UrlGenerator generator;
  private JsonBasedUserStore userStore;
  private Authorizer authorizer;

  private JsonCrudServiceBuilder() {

  }

  public static JsonCrudServiceBuilder newJsonCrudService() {
    return new JsonCrudServiceBuilder();
  }

  public TinkerpopJsonCrudService forGraph(Graph graph) {
    if (vres == null) {
      vres = new Vres.Builder()
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

    return new TinkerpopJsonCrudService(graphWrapper, vres, handleAdder, userStore, generator, generator,
      generator, clock);//, authorizer);
  }

  public JsonCrudServiceBuilder withClock(Clock clock) {
    this.clock = clock;
    return this;
  }

  public JsonCrudServiceBuilder withVres(Vres vres) {
    this.vres = vres;
    return this;
  }

  public JsonCrudServiceBuilder withUrlGenerator(UrlGenerator generator) {
    this.generator = generator;
    return this;
  }

  public JsonCrudServiceBuilder withHandleAdder(HandleAdder handleAdder) {
    this.handleAdder = handleAdder;
    return this;
  }

  public JsonCrudServiceBuilder withAuthorizer(Authorizer authorizer) {
    this.authorizer = authorizer;
    return this;
  }
}
