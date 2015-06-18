package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

public class DomainEntityConversionVerifier<T extends DomainEntity> extends EntityConversionVerifier<T> {

  private final int revision;

  public DomainEntityConversionVerifier(Class<T> type, MongoStorage mongoStorage, GraphStorage graphStorage, int revision) {
    this(type, mongoStorage, graphStorage, new PropertyVerifier(), revision);
  }

  DomainEntityConversionVerifier(Class<T> type, MongoStorage mongoStorage, GraphStorage graphStorage, PropertyVerifier propertyVerifier, int revision) {
    super(type, mongoStorage, graphStorage, propertyVerifier);
    this.revision = revision;
  }

  @Override
  protected T getOldItem(String oldId) throws StorageException {
    T revEntity = mongoStorage.getRevision(type, oldId, revision);

    return revEntity != null ? revEntity : mongoStorage.getEntity(type, oldId);
  }

  @Override
  protected T getNewItem(String newId) throws StorageException {
    T revEntity = graphStorage.getDomainEntityRevision(type, newId, revision);
    return revEntity != null ? revEntity : graphStorage.getEntity(type, newId);
  }

}
