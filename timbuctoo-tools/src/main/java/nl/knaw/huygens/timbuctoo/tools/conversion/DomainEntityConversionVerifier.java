package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

public class DomainEntityConversionVerifier<T extends DomainEntity> extends AbstractEntityConversionVerifier<T> {

  protected final int revision;

  public DomainEntityConversionVerifier(Class<T> type, MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage, int revision) {
    this(type, mongoStorage, graphStorage, new PropertyVerifier(), revision);
  }

  DomainEntityConversionVerifier(Class<T> type, MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage, PropertyVerifier propertyVerifier, int revision) {
    super(type, mongoStorage, graphStorage, propertyVerifier);
    this.revision = revision;
  }

  @Override
  protected T getOldItem(String oldId) throws StorageException {
    T revEntity = mongoStorage.getRevision(type, oldId, revision);

    return revEntity != null ? revEntity : mongoStorage.getEntity(type, oldId);
  }
}
