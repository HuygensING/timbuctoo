package nl.knaw.huygens.timbuctoo.model.vre.vres;

import nl.knaw.huygens.timbuctoo.database.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;

import java.util.Map;
import java.util.Optional;

public class DatabaseConfiguredVres implements Vres {

  private final TransactionEnforcer transactionEnforcer;
  private Vres loadedInstance;

  public DatabaseConfiguredVres(TransactionEnforcer transactionEnforcer) {
    this.transactionEnforcer = transactionEnforcer;
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
    try (DataStoreOperations db = transactionEnforcer.start()) {
      loadedInstance = db.loadVres();
    }
  }
}
