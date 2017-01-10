package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.core.TimbuctooActions;
import nl.knaw.huygens.timbuctoo.experimental.womenwriters.WomenWritersJsonCrudService;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.UserStore;

public class CrudServiceFactory {

  private final Vres vres;
  private final UserStore userStore;
  private final UrlGenerator relationUrlFor;

  public CrudServiceFactory(Vres vres, UserStore userStore, UrlGenerator relationUrlFor) {
    this.vres = vres;
    this.userStore = userStore;
    this.relationUrlFor = relationUrlFor;
  }

  public JsonCrudService newJsonCrudService(TimbuctooActions timbuctooActions) {
    return new JsonCrudService(vres, userStore, relationUrlFor, timbuctooActions);
  }

  public WomenWritersJsonCrudService newWomenWritersJsonCrudService(TimbuctooActions timbuctooActions) {
    return new WomenWritersJsonCrudService(vres, userStore, relationUrlFor, timbuctooActions);
  }


}
