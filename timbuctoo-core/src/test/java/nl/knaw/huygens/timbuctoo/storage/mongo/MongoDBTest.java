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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.rules.ExpectedException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoDBTest {

  private static final boolean THROW_EXCEPTION = true;
  private static final boolean NO_EXCEPTION = false;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Verifies the cause of a storage exception.
   */
  private static class CauseMatcher extends TypeSafeMatcher<Exception> {
    @Override
    public void describeTo(Description description) {
      description.appendText("StorageException with throwable");
    }

    @Override
    public boolean matchesSafely(Exception item) {
      Throwable cause = item.getCause();
      return cause != null && cause.getClass() == MongoException.class;
    }
  }

  private Mongo mongo;
  private DB db;
  private MongoDB mongoDB;
  private DBCollection dbCollection;

  private void setupMongo(boolean throwException) {
    mongo = mock(Mongo.class);
    db = mock(DB.class);
    mongoDB = new MongoDB(mongo, db);
    dbCollection = mock(DBCollection.class);

    if (throwException) {
      thrown.expect(StorageException.class);
      thrown.expect(new CauseMatcher());
      doThrow(MongoException.class).when(dbCollection).count();
      doThrow(MongoException.class).when(dbCollection).createIndex(any(DBObject.class), any(DBObject.class));
      doThrow(MongoException.class).when(dbCollection).find(any(DBObject.class));
      doThrow(MongoException.class).when(dbCollection).findOne(any(DBObject.class));
      doThrow(MongoException.class).when(dbCollection).getStats();
      doThrow(MongoException.class).when(dbCollection).remove(any(DBObject.class));
    }
  }

  // count

  @Test
  public void testCountNoException() throws StorageException {
    testCount(NO_EXCEPTION);
  }

  @Test
  public void testCountThrowException() throws StorageException {
    testCount(THROW_EXCEPTION);
  }

  private void testCount(boolean throwException) throws StorageException {
    setupMongo(throwException);
    try {
      mongoDB.count(dbCollection);
    } finally {
      verify(dbCollection).count();
    }
  }

  // ensureIndex

  @Test
  public void testCreateIndexNoException() throws StorageException {
    testCreateIndex(NO_EXCEPTION);
  }

  @Test
  public void testCreateIndexThrowsException() throws StorageException {
    testCreateIndex(THROW_EXCEPTION);
  }

  private void testCreateIndex(boolean throwException) throws StorageException {
    setupMongo(throwException);
    DBObject keys = new BasicDBObject();
    DBObject options = new BasicDBObject();
    try {
      mongoDB.createIndex(dbCollection, keys, options);
    } finally {
      verify(dbCollection).createIndex(keys, options);
    }
  }

  // find

  @Test
  public void testFindNoException() throws StorageException {
    testFind(NO_EXCEPTION);
  }

  @Test
  public void testFindThrowsException() throws StorageException {
    testFindOne(THROW_EXCEPTION);
  }

  public void testFind(boolean throwException) throws StorageException {
    setupMongo(throwException);
    DBObject query = new BasicDBObject();
    try {
      mongoDB.find(dbCollection, query);
    } finally {
      verify(dbCollection).find(query);
    }
  }

  // findOne

  @Test
  public void testFindOneNoException() throws StorageException {
    testFindOne(NO_EXCEPTION);
  }

  @Test
  public void testFindOneThrowsException() throws StorageException {
    testFindOne(THROW_EXCEPTION);
  }

  public void testFindOne(boolean throwException) throws StorageException {
    setupMongo(throwException);
    DBObject query = new BasicDBObject();
    try {
      mongoDB.findOne(dbCollection, query);
    } finally {
      verify(dbCollection).findOne(query);
    }
  }

  // getStats

  @Test
  public void testGetStatsNoException() throws StorageException {
    testGetStats(NO_EXCEPTION);
  }

  @Test
  public void testGetStatsThrowException() throws StorageException {
    testGetStats(THROW_EXCEPTION);
  }

  private void testGetStats(boolean throwException) throws StorageException {
    setupMongo(throwException);
    try {
      mongoDB.getStats(dbCollection);
    } finally {
      verify(dbCollection).getStats();
    }
  }

  // remove

  @Test
  public void testRemoveNoException() throws StorageException {
    testRemove(NO_EXCEPTION);
  }

  @Test
  public void testRemoveThrowsException() throws StorageException {
    testRemove(THROW_EXCEPTION);
  }

  private void testRemove(boolean throwException) throws StorageException {
    setupMongo(throwException);
    DBObject query = new BasicDBObject();
    try {
      mongoDB.remove(dbCollection, query);
    } finally {
      verify(dbCollection).remove(query);
    }
  }

}
