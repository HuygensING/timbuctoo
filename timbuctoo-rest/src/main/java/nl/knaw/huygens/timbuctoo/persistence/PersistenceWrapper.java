package nl.knaw.huygens.timbuctoo.persistence;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class PersistenceWrapper {

  private final PersistenceManager manager;
  private final String baseUrl;
  private final TypeRegistry typeRegistry;

  @Inject
  public PersistenceWrapper(String baseUrl, PersistenceManager persistenceManager, TypeRegistry typeRegistry) {
    this.baseUrl = CharMatcher.is('/').trimTrailingFrom(baseUrl);
    this.manager = persistenceManager;
    Preconditions.checkNotNull(this.manager);
    this.typeRegistry = typeRegistry;
  }

  public String persistURL(String url) throws PersistenceException {
    return manager.persistURL(url);
  }

  public String getURLValue(String persistentId) throws PersistenceException {
    return manager.getPersistedURL(persistentId);
  }

  public String getPersistentURL(String persistentId) {
    return manager.getPersistentURL(persistentId);
  }

  public String persistObject(Class<? extends Entity> type, String objectId) throws PersistenceException {
    String url = createURL(type, objectId);
    return manager.persistURL(url);
  }

  public void deletePersistentId(String persistentId) throws PersistenceException {
    manager.deletePersistentId(persistentId);
  }

  private String createURL(Class<? extends Entity> type, String id) {
    String collection = typeRegistry.getXNameForType(type);
    return Joiner.on('/').join(baseUrl, Paths.DOMAIN_PREFIX, collection, id);
  }
}
