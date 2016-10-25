package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.database.TimbuctooActions;
import nl.knaw.huygens.timbuctoo.database.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.experimental.womenwriters.WomenWritersJsonCrudService;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.UserStore;

import java.time.Clock;

public class CrudServiceFactory {

  private Authorizer authorizer;
  private TransactionEnforcer transactionEnforcer;
  private Clock clock;
  private HandleAdder handleAdder;
  private Vres vres;
  private UserStore userStore;
  private UrlGenerator relationUrlFor;

  public CrudServiceFactory(Authorizer authorizer, TransactionEnforcer transactionEnforcer, Clock clock,
                            HandleAdder handleAdder, Vres vres, UserStore userStore, UrlGenerator relationUrlFor) {
    this.authorizer = authorizer;
    this.transactionEnforcer = transactionEnforcer;
    this.clock = clock;
    this.handleAdder = handleAdder;
    this.vres = vres;
    this.userStore = userStore;
    this.relationUrlFor = relationUrlFor;
  }

  public JsonCrudService newJsonCrudService() {
    TimbuctooActions timbuctooActions = createTimbuctooActions();
    return new JsonCrudService(vres, userStore, relationUrlFor, clock, timbuctooActions);
  }

  private TimbuctooActions createTimbuctooActions() {
    return new TimbuctooActions(authorizer, transactionEnforcer, clock, handleAdder);
  }

  public WomenWritersJsonCrudService newWomenWritersJsonCrudService() {
    return new WomenWritersJsonCrudService(vres, userStore, relationUrlFor, createTimbuctooActions());
  }
}
