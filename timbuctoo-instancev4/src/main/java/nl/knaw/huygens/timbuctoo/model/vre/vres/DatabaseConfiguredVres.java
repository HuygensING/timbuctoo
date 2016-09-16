package nl.knaw.huygens.timbuctoo.model.vre.vres;

import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;

import java.util.Map;
import java.util.Optional;

public class DatabaseConfiguredVres implements Vres {

  private final DataAccess dataAccess;
  private Vres loadedInstance;

  public DatabaseConfiguredVres(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public Optional<Collection> getCollection(String collection) {
    return getLoadedInstance().getCollection(collection);
  }

  public Optional<Collection> getCollectionForType(String type) {
    return getLoadedInstance().getCollectionForType(type);
  }

  public Vre getVre(String vre) {
    return getLoadedInstance().getVre(vre);
  }

  public Map<String, Vre> getVres() {
    return getLoadedInstance().getVres();
  }

  private Vres getLoadedInstance() {
    if (loadedInstance != null) {
      return loadedInstance;
    }
    reload();
    return loadedInstance;
  }

  public void reload() {
    try (DataAccess.DataAccessMethods db = dataAccess.start()) {
      loadedInstance = db.loadVres();
    }
  }
}
