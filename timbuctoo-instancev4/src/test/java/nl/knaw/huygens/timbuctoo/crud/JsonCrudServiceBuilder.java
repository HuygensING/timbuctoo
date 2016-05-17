package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.crud.changelistener.AddLabelChangeListener;
import nl.knaw.huygens.timbuctoo.crud.changelistener.CompositeChangeListener;
import nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes;
import nl.knaw.huygens.timbuctoo.model.vre.CollectionBuilder;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.net.URI;
import java.time.Clock;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.util.AuthorizerHelper.anyUserIsAllowedToWriteAnyCollectionAuthorizer;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonCrudServiceBuilder {
  private Vres vres;
  private Clock clock;
  private HandleAdder handleAdder;
  private UrlGenerator relationUrlGenerator;
  private UrlGenerator autoCompleteUrlGenerator;
  private UserStore userStore;
  private Authorizer authorizer;
  private GraphWrapper graphWrapper = null;
  private UrlGenerator handleUrlGenerator;
  private ChangeListener changeListener = new CompositeChangeListener(
          new AddLabelChangeListener()
  );

  private JsonCrudServiceBuilder() {
    vres = new Vres.Builder()
      .withVre("WomenWriters", "ww", vre -> vre
        .withCollection("wwdocuments")
        .withCollection("wwkeywords", c -> c
          .withDisplayName(PropertyTypes.localProperty("displayName"))
        )
        .withCollection("wwrelations", CollectionBuilder::isRelationCollection)
        .withCollection("wwlanguages", c -> c
          .withDisplayName(localProperty("wwlanguage_name"))
        )
        .withCollection("wwderivedrelations", c -> c
          .withDerivedRelation("hasPersonLanguage", () -> {
            P<String> isWw = new P<>((types, extra) -> types.contains("\"wwrelation\""), "");
            return __
              .outE("isCreatorOf").has("isLatest", true).not(has("isDeleted", true)).has("types", isWw).inV()
              .outE("hasWorkLanguage").has("isLatest", true).not(has("isDeleted", true)).has("types", isWw).inV();
          })
        )
        .withCollection("wwdisplaynames", c -> c
          .withDisplayName(PropertyTypes.localProperty("wwperson_displayName"))
        )
        .withCollection("wwpersons", c -> c
          .withProperty("name", localProperty("wwperson_name"))
          .withDisplayName(PropertyTypes.localProperty("displayName"))
        )
      )
      .build();

    relationUrlGenerator = (collection, id, rev) -> URI.create("http://example.com/");
    clock = Clock.systemDefaultZone();
    handleAdder = mock(HandleAdder.class);
    handleUrlGenerator = (collection, id, rev) -> URI.create("http://example.com/handleUrl");
    autoCompleteUrlGenerator = (collection, id, rev) -> URI.create("http://example.com/autocomplete");
    relationUrlGenerator = (collection, id, rev) -> URI.create("http://example.com/relationUrl");
    authorizer = anyUserIsAllowedToWriteAnyCollectionAuthorizer();
  }

  public static JsonCrudServiceBuilder newJsonCrudService() {
    return new JsonCrudServiceBuilder();
  }

  public TinkerpopJsonCrudService build() {
    return new TinkerpopJsonCrudService(graphWrapper, vres, handleAdder, userStore, handleUrlGenerator,
      autoCompleteUrlGenerator, relationUrlGenerator, clock, changeListener, authorizer);
  }

  public TinkerpopJsonCrudService forGraph(Graph graph) {
    if (this.graphWrapper != null) {
      throw new RuntimeException("Use .build() when specifying a custom graphWrapper");
    } else {
      graphWrapper = mock(GraphWrapper.class);
      when(graphWrapper.getGraph()).thenReturn(graph);
    }

    return build();
  }

  public JsonCrudServiceBuilder withClock(Clock clock) {
    this.clock = clock;
    return this;
  }

  public JsonCrudServiceBuilder withVres(Vres vres) {
    this.vres = vres;
    return this;
  }

  public JsonCrudServiceBuilder withRelationUrlGenerator(UrlGenerator generator) {
    this.relationUrlGenerator = generator;
    return this;
  }

  public JsonCrudServiceBuilder withAutocompletenUrlGenerator(UrlGenerator generator) {
    this.autoCompleteUrlGenerator = generator;
    return this;
  }

  public JsonCrudServiceBuilder withHandleAdder(UrlGenerator handleUrlGenerator, HandleAdder handleAdder) {
    this.handleUrlGenerator = handleUrlGenerator;
    this.handleAdder = handleAdder;
    return this;
  }

  public JsonCrudServiceBuilder withAuthorizer(Authorizer authorizer) {
    this.authorizer = authorizer;
    return this;
  }

  public JsonCrudServiceBuilder withUserStore(UserStore userStore) {
    this.userStore = userStore;
    return this;
  }

  public JsonCrudServiceBuilder withGraphWrapper(GraphWrapper wrapper) {
    this.graphWrapper = wrapper;
    return this;
  }

  public GraphWrapper getGraphWrapperMock() {
    return graphWrapper;
  }

  public JsonCrudServiceBuilder withChangeListener(ChangeListener changeListener) {
    this.changeListener = changeListener;
    return this;
  }
}
