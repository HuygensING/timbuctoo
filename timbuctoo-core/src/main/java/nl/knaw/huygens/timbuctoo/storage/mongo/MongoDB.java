package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.net.UnknownHostException;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;

/**
 * Encapsulates the Mongo database.
 */
@Singleton
public class MongoDB {

  private static final Logger LOG = LoggerFactory.getLogger(MongoDB.class);

  private final Mongo mongo;
  private final DB db;

  @Inject
  public MongoDB(Configuration config) throws UnknownHostException {
    MongoOptions options = new MongoOptions();
    options.safe = true;

    String host = config.getSetting("database.host", "localhost");
    int port = config.getIntSetting("database.port", 27017);
    mongo = new Mongo(new ServerAddress(host, port), options);

    String dbName = config.getSetting("database.name");
    db = mongo.getDB(dbName);

    String user = config.getSetting("database.user");
    if (!user.isEmpty()) {
      String password = config.getSetting("database.password");
      db.authenticate(user, password.toCharArray());
    }
  }

  @VisibleForTesting
  MongoDB(Mongo mongo, DB db) {
    this.mongo = mongo;
    this.db = db;
  }

  public void dropDatabase() {
    try {
      LOG.info("Dropping database '{}'", db.getName());
      db.dropDatabase();
      LOG.info("Dropped database");
    } catch (MongoException e) {
      LOG.error(e.getMessage());
    }
  }

  public void close() {
    db.cleanCursors(true);
    mongo.close();
    LOG.info("Closed");
  }

  /**
   * Closes the specified cursor.
   */
  public void closeCursor(DBCursor cursor) throws StorageException {
    if (cursor != null) {
      try {
        cursor.close();
      } catch (MongoException e) {
        throw new StorageException(e);
      }
    }
  }

  /**
   * Gets a collection with the specified name.
   * If the collection does not exist, a new one is created.
   */
  public DBCollection getCollection(String name) {
    return db.getCollection(name);
  }

  /**
   * Returns the number of documents in the specified collection.
   */
  public long count(DBCollection collection) throws StorageException {
    try {
      return collection.count();
    } catch (MongoException e) {
      throw new StorageException(e);
    }
  }

  /**
   * Returns statistics for the specified collection.
   */
  public DBObject getStats(DBCollection collection) throws StorageException {
    try {
      return collection.getStats();
    } catch (MongoException e) {
      throw new StorageException(e);
    }
  }

  /**
   * Creates an index on a set of fields, if one does not already exist,
   * using the specified options.
   */
  public void ensureIndex(DBCollection collection, DBObject keys, DBObject options) throws StorageException {
    try {
      collection.ensureIndex(keys, options);
    } catch (MongoException e) {
      throw new StorageException(e);
    }
  }

  /**
   * Inserts a document into the database.
   */
  public void insert(DBCollection collection, String id, DBObject document) throws StorageException {
    try {
      collection.insert(document);
      if (collection.find(new BasicDBObject("_id", id)) == null) {
        LOG.error("Failed to insert ({}, {})", collection.getName(), id);
        throw new StorageException("Insert failed");
      }
    } catch (MongoException e) {
      throw new StorageException(e);
    }
  }

  /**
   * Updates a document in the database.
   */
  public void update(DBCollection collection, DBObject query, DBObject document) throws UpdateException, StorageException {
    try {
      WriteResult writeResult = collection.update(query, document);
      if (writeResult.getN() == 0) {
        LOG.error("Failed to update {}", query);
        throw new UpdateException("Update failed");
      }
    } catch (MongoException e) {
      throw new StorageException(e);
    }
  }

  public DBCursor find(DBCollection collection, DBObject query) throws StorageException {
    try {
      return collection.find(query);
    } catch (MongoException e) {
      throw new StorageException(e);
    }
  }

  public DBObject findOne(DBCollection collection, DBObject query) throws StorageException {
    try {
      return collection.findOne(query);
    } catch (MongoException e) {
      throw new StorageException(e);
    }
  }

  /**
   * Returns {@code true} if the specified collection contains an item
   * that satisfies the specified query, {@code false} otherwise.
   */
  public boolean exist(DBCollection collection, DBObject query) throws StorageException {
    DBCursor cursor = null;
    try {
      cursor = collection.find(query);
      return cursor.hasNext();
    } catch (MongoException e) {
      throw new StorageException(e);
    } finally {
      closeCursor(cursor);
    }
  }

  /**
   * Removes documents from the database.
   */
  public int remove(DBCollection collection, DBObject query) throws StorageException {
    try {
      WriteResult result = collection.remove(query);
      return (result != null) ? result.getN() : 0;
    } catch (MongoException e) {
      throw new StorageException(e);
    }
  }

}
