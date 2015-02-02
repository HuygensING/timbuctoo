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

import static nl.knaw.huygens.timbuctoo.storage.XProperties.propertyName;
import static nl.knaw.huygens.timbuctoo.storage.mongo.JacksonDBObjectMatcher.jacksonDBObjectMatcherHasObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.Properties;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mongojack.internal.stream.JacksonDBObject;

import test.variation.model.BaseVariationDomainEntity;
import test.variation.model.TestSystemEntity;
import test.variation.model.projecta.ProjectADomainEntity;
import test.variation.model.projecta.ProjectARelation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
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
    Properties properties = new MongoProperties();
    EntityInducer inducer = new EntityInducer(properties);
    EntityReducer reducer = new EntityReducer(properties, registry);
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
  @Test
  public void deleteDomainEntityRemovesTheWholeEntityFromTheDatabase() throws Exception {
    Class<? extends DomainEntity> type = BaseVariationDomainEntity.class;

    // Say that the item is deleted
    when(mongoDB.remove(dbCollection, queries.selectById(DEFAULT_ID))).thenReturn(1);

    storage.deleteDomainEntity(type, DEFAULT_ID, new Change());

    verify(mongoDB).remove(dbCollection, queries.selectById(DEFAULT_ID));
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteDomainEntityThrowsAnIllegalArgumentExceptionIfTheTypeIsNotAPrimitiveType() throws Exception {
    Class<? extends DomainEntity> type = ProjectADomainEntity.class;
    try {
      storage.deleteDomainEntity(type, DEFAULT_ID, new Change());
    } finally {
      verifyZeroInteractions(mongoDB);
    }
  }

  @Test(expected = NoSuchEntityException.class)
  public void deleteDomainEntityThrowsANoSuchEntityExceptionWhenTheEntityCannotBeFound() throws Exception {
    Class<? extends DomainEntity> type = BaseVariationDomainEntity.class;

    try {
      storage.deleteDomainEntity(type, DEFAULT_ID, new Change());
    } finally {
      verify(mongoDB).remove(dbCollection, queries.selectById(DEFAULT_ID));
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void deleteVariationRemovesTheVariantFromTheDatabase() throws Exception {
    Class<ProjectADomainEntity> type = ProjectADomainEntity.class;
    String prefix = TypeNames.getInternalName(type);
    String basePrefix = TypeNames.getInternalName(TypeRegistry.getBaseClass(type));

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    ArrayNode variationNode = objectMapper.createArrayNode();
    variationNode.add(prefix)//
        .add(basePrefix);

    node.put(createFieldName(prefix, "projectAGeneralTestDocValue"), "test") //
        .put(createFieldName(prefix, "name"), "test") //
        .put(createFieldName(prefix, "generalTestDocValue"), "test") //
        .put(createFieldName(basePrefix, "name"), "test") //
        .put(createFieldName(basePrefix, "generalTestDocValue"), "test") //
        .put("_id", DEFAULT_ID) //
        .put("^pid", "testPid")//
        .put("^rev", 1)//
        .put("^deleted", false)//
        .put("^variations", variationNode);

    DBObject serializedEntity = new JacksonDBObject<JsonNode>(node, JsonNode.class);
    when(dbCollection.findOne(queries.selectById(DEFAULT_ID))).thenReturn(serializedEntity);

    // action
    Change change = new Change();
    storage.deleteVariation(type, DEFAULT_ID, change);

    ObjectNode expected = objectMapper.createObjectNode();
    ArrayNode expectedVariationNode = objectMapper.createArrayNode();
    expectedVariationNode.add(basePrefix);
    expected.put(createFieldName(basePrefix, "name"), "test") //
        .put(createFieldName(basePrefix, "generalTestDocValue"), "test") //
        .put("_id", DEFAULT_ID) //
        .put("^rev", 2)//
        .put("^deleted", false)//
        .put("^variations", expectedVariationNode);
    expected.put("^modified", objectMapper.valueToTree(change));

    // verify    
    @SuppressWarnings("rawtypes")
    ArgumentCaptor<JacksonDBObject> entityCaptor = ArgumentCaptor.forClass(JacksonDBObject.class);
    verify(mongoDB).update(any(DBCollection.class), any(DBObject.class), entityCaptor.capture());
    assertThat((JacksonDBObject<JsonNode>) entityCaptor.getValue(), jacksonDBObjectMatcherHasObject(expected));
  }

  private String createFieldName(String prefix, String fieldName) {
    return String.format("%s:%s", prefix, fieldName);
  }

  @Test(expected = NoSuchEntityException.class)
  public void deleteVariationThrowsANoSuchEntityExceptionWhenTheVariationIsNotFound() throws Exception {
    Class<ProjectADomainEntity> type = ProjectADomainEntity.class;
    String basePrefix = TypeNames.getInternalName(TypeRegistry.getBaseClass(type));

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    ArrayNode variationNode = objectMapper.createArrayNode();
    variationNode.add(basePrefix);

    node.put(createFieldName(basePrefix, "name"), "test") //
        .put(createFieldName(basePrefix, "generalTestDocValue"), "test") //
        .put("_id", DEFAULT_ID) //
        .put("^pid", "testPid")//
        .put("^rev", 1)//
        .put("^deleted", false)//
        .put("^variations", variationNode);

    when(mongoDB.findOne(dbCollection, queries.selectById(DEFAULT_ID))).thenReturn(new JacksonDBObject<JsonNode>(node, JsonNode.class));

    try {
      storage.deleteVariation(type, DEFAULT_ID, new Change());
    } finally {
      verify(dbCollection).findOne(queries.selectById(DEFAULT_ID));
      verify(mongoDB, never()).update(any(DBCollection.class), any(DBObject.class), any(DBObject.class));
    }
  }

  @Test(expected = NoSuchEntityException.class)
  public void deleteVariationThrowsANoSuchEntityExceptionWhenTheEntityIsNotFound() throws Exception {
    Class<ProjectADomainEntity> type = ProjectADomainEntity.class;

    try {
      storage.deleteVariation(type, DEFAULT_ID, new Change());
    } finally {
      verify(dbCollection).findOne(queries.selectById(DEFAULT_ID));
      verify(mongoDB, never()).update(any(DBCollection.class), any(DBObject.class), any(DBObject.class));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteVariationThrowsAnIllegalArgumentExceptionWhenTheTypeIsPrimitive() throws Exception {
    Class<? extends DomainEntity> primitiveType = BaseVariationDomainEntity.class;

    try {
      storage.deleteVariation(primitiveType, DEFAULT_ID, new Change());
    } finally {
      verifyZeroInteractions(mongoDB);
    }

  }

  @Test
  public void deleteRelationsOfEntityRemovesTheRelationsOfEntityFromTheDatabase() throws Exception {
    // action
    storage.deleteRelationsOfEntity(Relation.class, DEFAULT_ID);

    // verify
    verify(mongoDB).remove(dbCollection, queries.selectRelationsByEntityId(DEFAULT_ID));
  }

  @Test
  public void declineRelationsOfEntitySetAcceptedToFalseForTheSelectedVariationsAndSetsThePIDToNullAndIncreasesTheRevision() throws Exception {
    // setup
    Class<ProjectARelation> type = ProjectARelation.class;
    String propertyName = String.format("%s:accepted", TypeNames.getInternalName(type));

    Map<String, Object> propertiesWithValues = Maps.newHashMap();
    propertiesWithValues.put(propertyName, false);
    propertiesWithValues.put(DomainEntity.PID, null);

    // action
    storage.declineRelationsOfEntity(type, DEFAULT_ID);

    // verify
    DBObject setQueryPart = queries.setPropertiesToValue(propertiesWithValues);
    DBObject incQueryPArt = queries.incrementRevision();
    BasicDBObject query = new BasicDBObject();
    query.putAll(setQueryPart);
    query.putAll(incQueryPArt);
    verify(dbCollection).update(queries.selectRelationsByEntityId(DEFAULT_ID), query, false, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void declineRelationsOfEntityRemovesThrowsAnIllegalArgumentExceptionWhenTheRelationTypeIsPrimitive() throws Exception {
    try {
      storage.declineRelationsOfEntity(Relation.class, DEFAULT_ID);
    } finally {
      verifyZeroInteractions(mongoDB);
    }
  }

  // deleteNonPersistent

  @Test
  public void testDeleteNonPersistent() throws Exception {
    List<String> ids = Lists.newArrayList("TCD000000001", "TCD000000003", "TCD000000005");

    storage.deleteNonPersistent(BaseVariationDomainEntity.class, ids);

    DBObject query = queries.selectNonPersistent(ids);
    verify(mongoDB).remove(dbCollection, query);
  }

  @Test
  public void doesVariationExistReturnsTrueWhenTheDomainEntityContainsTheVariation() throws StorageException {
    Class<ProjectADomainEntity> type = ProjectADomainEntity.class;
    String prefix = TypeNames.getInternalName(type);
    String basePrefix = TypeNames.getInternalName(TypeRegistry.getBaseClass(type));

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    ArrayNode variationNode = objectMapper.createArrayNode();
    variationNode.add(prefix)//
        .add(basePrefix);

    node.put(createFieldName(prefix, "projectAGeneralTestDocValue"), "test") //
        .put(createFieldName(prefix, "name"), "test") //
        .put(createFieldName(prefix, "generalTestDocValue"), "test") //
        .put(createFieldName(basePrefix, "name"), "test") //
        .put(createFieldName(basePrefix, "generalTestDocValue"), "test") //
        .put("_id", DEFAULT_ID) //
        .put("^pid", "testPid")//
        .put("^rev", 1)//
        .put("^deleted", false)//
        .put("^variations", variationNode);

    when(dbCollection.findOne(queries.selectById(DEFAULT_ID))).thenReturn(new JacksonDBObject<JsonNode>(node, JsonNode.class));

    // action
    boolean exists = storage.doesVariationExist(type, DEFAULT_ID);

    assertThat(exists, is(equalTo(true)));
  }

  @Test
  public void doesVariationExistReturnsFalseWhenTheDomainEntityDoesNotContainTheVariation() throws Exception {
    Class<ProjectADomainEntity> type = ProjectADomainEntity.class;
    String basePrefix = TypeNames.getInternalName(TypeRegistry.getBaseClass(type));

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    ArrayNode variationNode = objectMapper.createArrayNode();
    variationNode.add(basePrefix);

    node.put(createFieldName(basePrefix, "name"), "test") //
        .put(createFieldName(basePrefix, "generalTestDocValue"), "test") //
        .put("_id", DEFAULT_ID) //
        .put("^pid", "testPid")//
        .put("^rev", 1)//
        .put("^deleted", false)//
        .put("^variations", variationNode);

    when(dbCollection.findOne(queries.selectById(DEFAULT_ID))).thenReturn(new JacksonDBObject<JsonNode>(node, JsonNode.class));

    // action
    boolean exists = storage.doesVariationExist(type, DEFAULT_ID);

    assertThat(exists, is(equalTo(false)));
  }

  @Test
  public void doesVariationExistReturnsFalseWhenTheDomainEntityDoesNotExist() throws Exception {
    Class<? extends DomainEntity> type = ProjectADomainEntity.class;
    when(dbCollection.findOne(queries.selectById(DEFAULT_ID))).thenReturn(null);

    // action
    boolean exists = storage.doesVariationExist(type, DEFAULT_ID);

    assertThat(exists, is(equalTo(false)));
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
