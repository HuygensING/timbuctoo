package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

/**
 * A class that checks if the RelationType is correctly converted.
 */
public class SystemEntityConversionVerifier<T extends SystemEntity> extends AbstractEntityConversionVerifier<T> {

  public SystemEntityConversionVerifier(Class<T> type, MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage) {
    this(type, mongoStorage, graphStorage, new PropertyVerifier());
  }

  public SystemEntityConversionVerifier(Class<T> type, MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage, PropertyVerifier propertyVerifier) {
    super(type, mongoStorage, graphStorage, propertyVerifier);
  }

  @Override
  protected T getOldItem(String oldId) throws StorageException {
    return mongoStorage.getEntity(type, oldId);
  }

}
