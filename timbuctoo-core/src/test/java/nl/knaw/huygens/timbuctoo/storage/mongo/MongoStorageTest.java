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

import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntity;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.mongodb.DBObject;

public class MongoStorageTest extends MongoStorageTestBase {

  private static TypeRegistry registry;

  @BeforeClass
  public static void setupRegistry() {
    registry = TypeRegistry.getInstance().init(TestSystemEntity.class.getPackage().getName());
  }

  @AfterClass
  public static void clearRegistry() {
    registry = null;
  }

  // ---------------------------------------------------------------------------

  private MongoStorage storage;

  @Override
  protected void setupStorage() {
    storage = new MongoStorage(registry, mongoDB, entityIds);
  }

  // ---------------------------------------------------------------------------

  // deleteSystemEntity

  @Test
  public void testDeleteSystemEntity() throws Exception {
    storage.deleteSystemEntity(TestSystemEntity.class, DEFAULT_ID);

    DBObject query =  queries.selectById(DEFAULT_ID);
    verify(mongoDB).remove(anyCollection, query);
  }

  // deleteSystemEntities

  @Test
  public void testDeleteSystemEntities() throws Exception {
    storage.deleteSystemEntities(TestSystemEntity.class);

    DBObject query = queries.selectAll();
    verify(mongoDB).remove(anyCollection, query);
  }

  // deleteByDate

  @Test
  public void testRemoveByDate() throws Exception {
    Date date = new Date();

    storage.deleteByDate(TestSystemEntity.class, "date", date);

    DBObject query = queries.selectByDate(TestSystemEntity.class, "date", date);
    verify(mongoDB).remove(anyCollection, query);
  }

  // deleteDomainEntity

  // deleteNonPersistent

  @Test
  public void testDeleteNonPersistent() throws Exception {
    List<String> ids = Lists.newArrayList("TCD000000001", "TCD000000003", "TCD000000005");

    storage.deleteNonPersistent(BaseDomainEntity.class, ids);

    DBObject query = queries.selectNonPersistent(ids);
    verify(mongoDB).remove(anyCollection, query);
  }

  // entityExists

  // getItem

  @Test
  public void testGetItemForSystemEntity() throws Exception {
    storage.getItem(TestSystemEntity.class, DEFAULT_ID);

    DBObject query = queries.selectById(DEFAULT_ID);
    // TODO verify MongoDB once it implements findOne()
    verify(anyCollection).findOne(query);
    // TODO verify call to EntityReducer
  }

  @Test
  public void testGetItemForDomainEntity() throws Exception {
    storage.getItem(BaseDomainEntity.class, DEFAULT_ID);

    DBObject query = queries.selectById(DEFAULT_ID);
    // TODO verify MongoDB once it implements findOne()
    verify(anyCollection).findOne(query);
    // TODO verify call to EntityReducer
  }

  // getEntities

  @Test
  public void testGetSystemEntities() throws Exception {
    storage.getEntities(TestSystemEntity.class);

    DBObject query = queries.selectAll();
    verify(mongoDB).find(anyCollection, query);
  }

  @Test
  public void testGetDomainEntities() throws Exception {
    storage.getEntities(BaseDomainEntity.class);

    DBObject query = queries.selectAll();
    verify(mongoDB).find(anyCollection, query);
  }

  // findItem

  @Test
  public void testFindItem() throws Exception {
    TestSystemEntity example = new TestSystemEntity().withName("doc1");

    storage.findItem(TestSystemEntity.class, example);

    DBObject query = queries.selectByProperty(TestSystemEntity.class, "name", "doc1");
    // TODO verify MongoDB once it implements findOne()
    verify(anyCollection).findOne(query);
    // TODO verify call to EntityReducer
  }

  // getAllVariations

  @Test
  public void testGetAllVariationsWithoutRelations() throws Exception {
    storage.getAllVariations(BaseDomainEntity.class, DEFAULT_ID);

    DBObject query = queries.selectById(DEFAULT_ID);
    verify(mongoDB).findOne(anyCollection, query);
    // TODO verify call to EntityReducer, in two separate tests, depending on number of results
  }

  // getRevision

  @Test
  public void testGetRevision() throws Exception {
    int revision = 42;

    storage.getRevision(BaseDomainEntity.class, DEFAULT_ID, revision);

    DBObject query = queries.selectById(DEFAULT_ID);
    DBObject projection = queries.getRevisionProjection(revision);
    verify(anyCollection).findOne(query, projection);
  }

  // getAllRevisions

  @Test
  public void testGetAllRevisions() throws Exception {
    storage.getAllRevisions(BaseDomainEntity.class, DEFAULT_ID);

    DBObject query = queries.selectById(DEFAULT_ID);
    verify(mongoDB).findOne(anyCollection, query);
    // TODO verify call to EntityReducer
  }

}
