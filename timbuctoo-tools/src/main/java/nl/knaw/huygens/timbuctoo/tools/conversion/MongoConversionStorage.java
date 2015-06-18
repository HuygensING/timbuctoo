package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;
import nl.knaw.huygens.timbuctoo.storage.Properties;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.mongo.EntityIds;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoDB;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoQueries;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.mongodb.DBObject;

public class MongoConversionStorage extends MongoStorage {

  private MongoQueries queries;

  @Inject
  public MongoConversionStorage(MongoDB mongoDB, EntityIds entityIds, Properties properties, EntityInducer inducer, EntityReducer reducer) {
    super(mongoDB, entityIds, properties, inducer, reducer);
    queries = new MongoQueries();
  }

  public <T extends DomainEntity> AllVersionVariationMap<T> getAllVersionVariationsMapOf(Class<T> type, String id) throws StorageException {
    DBObject selectById = queries.selectById(id);
    DBObject foundObject = getVersionCollection(type).findOne(selectById);

    if (foundObject != null) {
      JsonNode jsonNode = toJsonNode(foundObject);

      return AllVersionVariationMap.forVersionNode(type, jsonNode, reducer);
    } else {
      foundObject = getDBCollection(type).findOne(selectById);
      JsonNode jsonNode = toJsonNode(foundObject);

      return AllVersionVariationMap.forNormalNode(type, jsonNode, reducer);
    }
  }
}
