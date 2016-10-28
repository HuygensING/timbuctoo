package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.TimbuctooActions;
import nl.knaw.huygens.timbuctoo.database.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.experimental.womenwriters.WomenWritersJsonCrudService;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthorizer;
import nl.knaw.huygens.timbuctoo.security.JsonBasedUserStore;
import nl.knaw.huygens.timbuctoo.security.UserStore;

import java.time.Clock;
import java.util.function.Supplier;

public class CrudServiceFactory {

  private final Authorizer authorizer;
  private final Clock clock;
  private final HandleAdder handleAdder;
  private final Vres vres;
  private final UserStore userStore;
  private final Supplier<DataStoreOperations> datastore;
  private final UrlGenerator relationUrlFor;

  public CrudServiceFactory(JsonBasedAuthorizer authorizer, Clock clock,
                            HandleAdder handleAdder, Vres vres, JsonBasedUserStore userStore,
                            UrlGenerator relationUrlFor, Supplier<DataStoreOperations> datastore) {
    this.authorizer = authorizer;
    this.clock = clock;
    this.handleAdder = handleAdder;
    this.vres = vres;
    this.userStore = userStore;
    this.relationUrlFor = relationUrlFor;
    this.datastore = datastore;
  }

  public JsonCrudService newJsonCrudService(TimbuctooActions timbuctooActions) {
    return new JsonCrudService(vres, userStore, relationUrlFor, clock, timbuctooActions);
  }

  public WomenWritersJsonCrudService newWomenWritersJsonCrudService(TimbuctooActions timbuctooActions) {
    return new WomenWritersJsonCrudService(vres, userStore, relationUrlFor, timbuctooActions);
  }


}
