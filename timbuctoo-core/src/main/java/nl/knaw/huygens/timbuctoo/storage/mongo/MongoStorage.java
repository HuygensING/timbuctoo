package nl.knaw.huygens.timbuctoo.storage.mongo;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.storage.BasicStorage;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class MongoStorage extends MongoStorageBase implements BasicStorage {

  public MongoStorage(TypeRegistry registry, Mongo mongo, DB db, String dbName) {
    super(registry, mongo, db, dbName);
  }

}
