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

import static nl.knaw.huygens.timbuctoo.storage.Properties.propertyName;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.variation.model.BaseVariationDomainEntity;
import test.variation.model.TestSystemEntity;
import test.variation.model.projecta.ProjectADomainEntity;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoStorageTest extends MongoStorageTestBase {

  private static TypeRegistry registry;

  @BeforeClass
  public static void setupRegistry() throws ModelException {
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
    EntityInducer inducer = new EntityInducer(new MongoPropertyInducer());
    EntityReducer reducer = new EntityReducer(new MongoPropertyReducer(), registry);
    storage = new MongoStorage(mongoDB, entityIds, inducer, reducer);
  }

  // ---------------------------------------------------------------------------

  // createIndex

  @Test
  public void testCreateIndexWithOneKey() throws Exception {
    storage.createIndex(true, TestSystemEntity.class, "name");

    DBObject keys = new BasicDBObject(propertyName(TestSystemEntity.class, "name"), 1);
    DBObject options = new BasicDBObject("unique", true);
    verify(mongoDB).createIndex(eq(dbCollection), eq(keys), eq(options));
  }

  @Test
  public void testCreateIndexWithTwoKeys() throws Exception {
    storage.createIndex(false, TestSystemEntity.class, "name", "^rev");

    DBObject keys = new BasicDBObject(propertyName(TestSystemEntity.class, "name"), 1) //
        .append(propertyName(TestSystemEntity.class, "^rev"), 1);
    DBObject options = new BasicDBObject("unique", false);
    verify(mongoDB).createIndex(eq(dbCollection), eq(keys), eq(options));
  }

  // deleteSystemEntity

  @Test
  public void testDeleteSystemEntity() throws Exception {
    storage.deleteSystemEntity(TestSystemEntity.class, DEFAULT_ID);

    DBObject query = queries.selectById(DEFAULT_ID);
    verify(mongoDB).remove(dbCollection, query);
  }

  // deleteSystemEntities

  @Test
  public void testDeleteSystemEntities() throws Exception {
    storage.deleteSystemEntities(TestSystemEntity.class);

    DBObject query = queries.selectAll();
    verify(mongoDB).remove(dbCollection, query);
  }

  // deleteByDate

  @Test
  public void testRemoveByDate() throws Exception {
    Date date = new Date();

    storage.deleteByModifiedDate(TestSystemEntity.class, date);

    DBObject query = queries.selectByModifiedDate(date);
    verify(mongoDB).remove(dbCollection, query);
  }

  // deleteDomainEntity

  // deleteNonPersistent

  @Test
  public void testDeleteNonPersistent() throws Exception {
    List<String> ids = Lists.newArrayList("TCD000000001", "TCD000000003", "TCD000000005");

    storage.deleteNonPersistent(BaseVariationDomainEntity.class, ids);

    DBObject query = queries.selectNonPersistent(ids);
    verify(mongoDB).remove(dbCollection, query);
  }

  // entityExists

  // getItem

  @Test
  public void testGetItemForSystemEntity() throws Exception {
    storage.getItem(TestSystemEntity.class, DEFAULT_ID);

    DBObject query = queries.selectById(DEFAULT_ID);
    verify(mongoDB).findOne(dbCollection, query);
    // TODO verify call to EntityReducer
  }

  @Test
  public void testGetItemForDomainEntity() throws Exception {
    storage.getItem(BaseVariationDomainEntity.class, DEFAULT_ID);

    DBObject query = queries.selectById(DEFAULT_ID);
    verify(mongoDB).findOne(dbCollection, query);
    // TODO verify call to EntityReducer
  }

  // getSystemEntities, getDomainEntities

  @Test
  public void testGetSystemEntities() throws Exception {
    storage.getSystemEntities(TestSystemEntity.class);

    DBObject query = queries.selectAll();
    verify(mongoDB).find(dbCollection, query);
  }

  @Test
  public void testGetPrimitiveDomainEntities() throws Exception {
    storage.getDomainEntities(BaseVariationDomainEntity.class);

    DBObject query = queries.selectAll();
    verify(mongoDB).find(dbCollection, query);
  }

  @Test
  public void testGetProjectDomainEntities() throws Exception {
    storage.getDomainEntities(ProjectADomainEntity.class);

    DBObject query = queries.selectVariation(ProjectADomainEntity.class);
    verify(mongoDB).find(dbCollection, query);
  }

  // getAllVariations

  @Test(expected = IllegalArgumentException.class)
  public void testGetAllVariationsOfNonPrimitive() throws Exception {
    storage.getAllVariations(ProjectADomainEntity.class, DEFAULT_ID);
  }

  @Test
  public void testGetAllVariationsWithoutRelations() throws Exception {
    storage.getAllVariations(BaseVariationDomainEntity.class, DEFAULT_ID);

    DBObject query = queries.selectById(DEFAULT_ID);
    verify(mongoDB).findOne(dbCollection, query);
    // TODO verify call to EntityReducer, in two separate tests, depending on number of results
  }

  // getRevision

  @Test
  public void testGetRevision() throws Exception {
    int revision = 42;

    storage.getRevision(BaseVariationDomainEntity.class, DEFAULT_ID, revision);

    DBObject query = queries.getRevisionFromVersionCollection(DEFAULT_ID, revision);
    DBObject projection = queries.getRevisionProjection(revision);
    verify(dbCollection).findOne(query, projection);
  }

  // getAllRevisions

  @Test
  public void testGetAllRevisions() throws Exception {
    storage.getAllRevisions(BaseVariationDomainEntity.class, DEFAULT_ID);

    DBObject query = queries.selectById(DEFAULT_ID);
    verify(mongoDB).findOne(dbCollection, query);
    // TODO verify call to EntityReducer
  }

  @Test
  public void testGetRelationsByType() throws Exception {
    // setup
    final String id1 = "id1";
    final String id2 = "id2";
    List<String> relationTypeIds = Lists.newArrayList(id1, id2);

    final Class<Relation> type = Relation.class;
    DBObject query = queries.selectByProperty(type, Relation.TYPE_ID, relationTypeIds);

    // action
    storage.getRelationsByType(type, relationTypeIds);

    // verify
    verify(mongoDB).find(dbCollection, query);
  }

}
