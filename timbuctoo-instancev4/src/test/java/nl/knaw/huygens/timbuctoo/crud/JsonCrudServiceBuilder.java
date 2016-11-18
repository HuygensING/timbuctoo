package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.database.AfterSuccessTaskExecutor;
import nl.knaw.huygens.timbuctoo.database.ChangeListener;
import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.PersistentUrlCreator;
import nl.knaw.huygens.timbuctoo.database.TimbuctooActions;
import nl.knaw.huygens.timbuctoo.database.changelistener.AddLabelChangeListener;
import nl.knaw.huygens.timbuctoo.database.changelistener.CompositeChangeListener;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.CollectionBuilder;
import nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;
import nl.knaw.huygens.timbuctoo.security.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.net.URI;
import java.time.Clock;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.personNames;
import static nl.knaw.huygens.timbuctoo.util.AuthorizerHelper.anyUserIsAllowedToWriteAnyCollectionAuthorizer;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Deprecated
public class JsonCrudServiceBuilder {
  private Vres vres;
  private Clock clock;
  private PersistentUrlCreator persistentUrlCreator;
  private UrlGenerator relationUrlGenerator;
  private UserStore userStore;
  private Authorizer authorizer;
  private GraphWrapper graphWrapper = null;
  private ChangeListener changeListener = new CompositeChangeListener(
    new AddLabelChangeListener()
  );
  private EntityFetcher entityFetcher;

  private JsonCrudServiceBuilder() {
    vres = new VresBuilder()
      .withVre("WomenWriters", "ww", vre -> vre
        .withCollection("wwdocuments", c -> c
          .withProperty("title", localProperty("wwdocument_title"))
          .withProperty("date", localProperty("wwdocument_date"))
        )
        .withCollection("wwkeywords", c -> c
          .withDisplayName(PropertyTypes.localProperty("displayName"))
          .withProperty("value", localProperty("wwkeyword_value"))
          .withProperty("type", localProperty("wwkeyword_type"))
        )
        .withCollection("wwrelations", CollectionBuilder::isRelationCollection)
        .withCollection("wwlanguages", c -> c
          .withDisplayName(localProperty("wwlanguage_name"))
          .withProperty("name", localProperty("wwlanguage_name"))
        )
        .withCollection("wwcollectives", c -> c
          .withDisplayName(localProperty("wwcollective_name"))
          .withProperty("name", localProperty("wwcollective_name"))
        )
        .withCollection("wwdisplaynames", c -> c
          .withDisplayName(PropertyTypes.localProperty("wwperson_displayName"))
        )
        .withCollection("wwpersons", c -> c
          .withProperty("name", localProperty("wwperson_name"))
          .withProperty("names", localProperty("wwperson_names", personNames))
          .withDisplayName(PropertyTypes.localProperty("displayName"))
        )
      )
      .build();

    relationUrlGenerator = (collection, id, rev) -> URI.create("http://example.com/");
    clock = Clock.systemDefaultZone();
    persistentUrlCreator = mock(PersistentUrlCreator.class);
    relationUrlGenerator = (collection, id, rev) -> URI.create("http://example.com/relationUrl");
    authorizer = anyUserIsAllowedToWriteAnyCollectionAuthorizer();
    userStore = mock(UserStore.class);

    try {
      when(userStore.userFor(any())).thenReturn(Optional.empty());
      when(userStore.userForId(any())).thenReturn(Optional.empty());
    } catch (AuthenticationUnavailableException e) {
      e.printStackTrace();
    }

    entityFetcher = new GremlinEntityFetcher();
  }

  public static JsonCrudServiceBuilder newJsonCrudService() {
    return new JsonCrudServiceBuilder();
  }

  public JsonCrudService build() {
    DataStoreOperations dataStoreOperations =
      new DataStoreOperations(graphWrapper, changeListener, entityFetcher, vres);

    return new JsonCrudService(vres, userStore,
      relationUrlGenerator,
      new TimbuctooActions(
        authorizer,
        clock,
        persistentUrlCreator,
        dataStoreOperations,
        new AfterSuccessTaskExecutor()
      )
    );
  }

  public JsonCrudService forGraph(Graph graph) {
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

  public JsonCrudServiceBuilder withChangeListener(ChangeListener changeListener) {
    this.changeListener = changeListener;
    return this;
  }
}
