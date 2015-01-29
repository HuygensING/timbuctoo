package nl.knaw.huygens.timbuctoo.storage;

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
import static org.junit.Assert.assertEquals;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoProperties;

import org.junit.Before;
import org.junit.Test;

import test.model.BaseDomainEntity;
import test.model.DomainEntityWithReferences;
import test.model.TestSystemEntity;
import test.model.projecta.SubADomainEntity;
import test.model.projectb.SubBDomainEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

public class EntityInducerTest {

  private final static String ID = "TEST042";
  private static final String PID = "test_pid";

  private EntityInducer inducer;
  private ObjectMapper mapper;

  @Before
  public void setup() throws Exception {
    inducer = new EntityInducer(new MongoProperties());
    mapper = new ObjectMapper();
  }

  private void addValue(Map<String, Object> map, String key, String value) {
    if (value != null) {
      map.put(key, value);
    }
  }

  private ObjectNode newSystemEntityTree(String id, String value1, String value2, String value3) {
    Map<String, Object> map = Maps.newTreeMap();
    addValue(map, "_id", id);
    map.put("^rev", 0);
    addValue(map, propertyName(TestSystemEntity.class, "value1"), value1);
    addValue(map, propertyName(TestSystemEntity.class, "value2"), value2);
    addValue(map, propertyName(TestSystemEntity.class, "value3"), value3);
    return mapper.valueToTree(map);
  }

  private Map<String, Object> newDomainEntityMap(String id, String pid) {
    Map<String, Object> map = Maps.newTreeMap();
    addValue(map, "_id", id);
    addValue(map, DomainEntity.PID, pid);
    map.put(DomainEntity.DELETED, false);
    map.put("^rev", 0);
    return map;
  }

  private Map<String, Object> newSubADomainEntityMap(String id, String pid, String bv1, String bv2, String sv1, String sv2, String sva) {
    Map<String, Object> map = newDomainEntityMap(id, pid);
    addValue(map, propertyName(BaseDomainEntity.class, "value1"), bv1);
    addValue(map, propertyName(BaseDomainEntity.class, "value2"), bv2);
    addValue(map, propertyName(SubADomainEntity.class, "value1"), sv1);
    addValue(map, propertyName(SubADomainEntity.class, "value2"), sv2);
    addValue(map, propertyName(SubADomainEntity.class, "valuea"), sva);
    return map;
  }

  private ObjectNode newSubADomainEntityTree(String id, String pid, String bv1, String bv2, String sv1, String sv2, String sva) {
    Map<String, Object> map = newSubADomainEntityMap(id, pid, bv1, bv2, sv1, sv2, sva);
    return mapper.valueToTree(map);
  }

  // --- new system entity ---------------------------------------------

  @Test
  public void induceSystemEntityAsPrimitive() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(ID, "v1", "v2", null);
    JsonNode expected = newSystemEntityTree(ID, "v1", "v2", null);
    assertEquals(expected, inducer.convertSystemEntityForAdd(TestSystemEntity.class, entity));
  }

  @Test(expected = IllegalArgumentException.class)
  public void induceSystemEntityAsSystemEntity() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(ID, "v1", "v2");
    inducer.convertSystemEntityForAdd(SystemEntity.class, entity);
  }

  // --- new primitive domain entitiy ----------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void inducePrimitiveDomainEntityAsPrimitive() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(ID, PID, "v1", "v2");
    inducer.convertDomainEntityForAdd(BaseDomainEntity.class, entity);
  }

  @Test(expected = IllegalArgumentException.class)
  public void inducePrimitiveDomainEntityAsDomainEntity() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(ID, PID, "v1", "v2");
    inducer.convertDomainEntityForAdd(DomainEntity.class, entity);
  }

  // --- new project domain entitiy ------------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void induceDerivedDomainEntityAsPrimitive() throws Exception {
    SubADomainEntity entity = new SubADomainEntity(ID, PID, "v1", "v2", "va");
    inducer.convertDomainEntityForAdd(BaseDomainEntity.class, entity);
  }

  @Test
  public void induceDerivedDomainEntityAsDerived() throws Exception {
    SubADomainEntity entity = new SubADomainEntity(ID, PID, "v1", "v2", "va");
    JsonNode expected = newSubADomainEntityTree(ID, PID, "v1", "v2", "v1", "v2", "va");
    assertEquals(expected, inducer.convertDomainEntityForAdd(SubADomainEntity.class, entity));
  }

  // --- old system entity ---------------------------------------------

  @Test
  public void updateSystemEntityWithPrimitiveView() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(ID, "updated", null, null);
    ObjectNode oldTree = newSystemEntityTree(ID, "v1", "v2", null);
    ObjectNode newTree = newSystemEntityTree(ID, "updated", null, null);
    assertEquals(newTree, inducer.convertSystemEntityForUpdate(TestSystemEntity.class, entity, oldTree));
  }

  @Test
  public void updateSystemEntityWithWrongView() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(ID, "updated", null, null);
    ObjectNode oldTree = newSystemEntityTree(ID, "v1", "v2", null);
    ObjectNode newTree = newSystemEntityTree(ID, "v1", "v2", null);
    assertEquals(newTree, inducer.convertSystemEntityForUpdate(SystemEntity.class, entity, oldTree));
  }

  // --- old domain entity ---------------------------------------------

  @Test
  public void updateOfProjectMustNotAffectPrimitive() throws Exception {
    // tree to be updated
    Map<String, Object> oldMap = newDomainEntityMap(ID, PID);
    oldMap.put(propertyName(BaseDomainEntity.class, "value1"), "v1");
    oldMap.put(propertyName(BaseDomainEntity.class, "value2"), "v2");
    oldMap.put(propertyName(SubADomainEntity.class, "value1"), "v1");
    oldMap.put(propertyName(SubADomainEntity.class, "value2"), "v2");
    oldMap.put(propertyName(SubADomainEntity.class, "valuea"), "va");
    ObjectNode oldTree = mapper.valueToTree(oldMap);

    // entity to update with
    SubADomainEntity entity = new SubADomainEntity(ID, PID);
    entity.setValue1("xv1");
    entity.setValue2(null);
    entity.setValuea("xva");

    // expected tree after update
    Map<String, Object> newMap = newDomainEntityMap(ID, PID);
    newMap.put(propertyName(BaseDomainEntity.class, "value1"), "v1");
    newMap.put(propertyName(BaseDomainEntity.class, "value2"), "v2");
    newMap.put(propertyName(SubADomainEntity.class, "value1"), "xv1");
    newMap.put(propertyName(SubADomainEntity.class, "valuea"), "xva");
    ObjectNode newTree = mapper.valueToTree(newMap);

    assertEquals(newTree, inducer.convertDomainEntityForUpdate(SubADomainEntity.class, entity, oldTree));
  }

  @Test
  public void updateOfProjectMustNotAffectSharedProperties() throws Exception {
    // tree to be updated
    Map<String, Object> oldMap = newDomainEntityMap(ID, PID);
    oldMap.put(propertyName(BaseDomainEntity.class, "^sharedValue"), "vs");
    ObjectNode oldTree = mapper.valueToTree(oldMap);

    // entity to update with
    SubADomainEntity entity = new SubADomainEntity(ID, PID);
    entity.setSharedValue("xvs");

    // expected tree after update
    Map<String, Object> newMap = newDomainEntityMap(ID, PID);
    newMap.put(propertyName(BaseDomainEntity.class, "^sharedValue"), "vs");
    ObjectNode newTree = mapper.valueToTree(newMap);

    assertEquals(newTree, inducer.convertDomainEntityForUpdate(SubADomainEntity.class, entity, oldTree));
  }

  @Test
  public void updateOfPrimitiveMustNotAffectProject() throws Exception {
    // tree to be updated
    Map<String, Object> oldMap = newDomainEntityMap(ID, PID);
    oldMap.put(propertyName(BaseDomainEntity.class, "value1"), "v1");
    oldMap.put(propertyName(BaseDomainEntity.class, "value2"), "v2");
    oldMap.put(propertyName(SubADomainEntity.class, "value1"), "v1");
    oldMap.put(propertyName(SubADomainEntity.class, "value2"), "v2");
    oldMap.put(propertyName(SubADomainEntity.class, "valuea"), "va");
    ObjectNode oldTree = mapper.valueToTree(oldMap);

    // entity to update with
    SubADomainEntity entity = new SubADomainEntity(ID, PID);
    entity.setValue1("x1");
    entity.setValue2(null);
    entity.setValuea("xa");

    // expected tree after update
    Map<String, Object> newMap = newDomainEntityMap(ID, PID);
    newMap.put(propertyName(BaseDomainEntity.class, "value1"), "x1");
    newMap.put(propertyName(SubADomainEntity.class, "value1"), "v1");
    newMap.put(propertyName(SubADomainEntity.class, "value2"), "v2");
    newMap.put(propertyName(SubADomainEntity.class, "valuea"), "va");
    ObjectNode newTree = mapper.valueToTree(newMap);

    assertEquals(newTree, inducer.convertDomainEntityForUpdate(BaseDomainEntity.class, entity, oldTree));
  }

  @Test
  public void updateOfPrimitiveMustAffectSharedProperties() throws Exception {
    // tree to be updated
    Map<String, Object> oldMap = newDomainEntityMap(ID, PID);
    oldMap.put(propertyName(BaseDomainEntity.class, "^sharedValue"), "vs");
    ObjectNode oldTree = mapper.valueToTree(oldMap);

    // entity to update with
    BaseDomainEntity entity = new BaseDomainEntity(ID);
    entity.setPid(null); // ignored
    entity.setSharedValue("xvs");

    // expected tree after update
    Map<String, Object> newMap = newDomainEntityMap(ID, PID);
    newMap.put(propertyName(BaseDomainEntity.class, "^sharedValue"), "xvs");
    ObjectNode newTree = mapper.valueToTree(newMap);

    assertEquals(newTree, inducer.convertDomainEntityForUpdate(BaseDomainEntity.class, entity, oldTree));
  }

  @Test
  public void updateOfAdministrativeMustNotAffectData() throws Exception {
    // tree to be updated
    Map<String, Object> oldMap = newDomainEntityMap(ID, PID);
    oldMap.put(propertyName(BaseDomainEntity.class, "value1"), "v1");
    oldMap.put(propertyName(BaseDomainEntity.class, "value2"), "v2");
    oldMap.put(propertyName(SubADomainEntity.class, "value1"), "v1");
    oldMap.put(propertyName(SubADomainEntity.class, "value2"), "v2");
    oldMap.put(propertyName(SubADomainEntity.class, "valuea"), "va");
    ObjectNode oldTree = mapper.valueToTree(oldMap);

    // entity to update with
    SubADomainEntity entity = new SubADomainEntity(ID, PID);
    entity.setValue1("xv1");
    entity.setValue2(null);
    entity.setValuea("xva");

    // expected tree after update
    Map<String, Object> newMap = newDomainEntityMap(ID, PID);
    newMap.put(propertyName(BaseDomainEntity.class, "value1"), "v1");
    newMap.put(propertyName(BaseDomainEntity.class, "value2"), "v2");
    newMap.put(propertyName(SubADomainEntity.class, "value1"), "v1");
    newMap.put(propertyName(SubADomainEntity.class, "value2"), "v2");
    newMap.put(propertyName(SubADomainEntity.class, "valuea"), "va");
    ObjectNode newTree = mapper.valueToTree(newMap);

    assertEquals(newTree, inducer.convertDomainEntityForUpdate(DomainEntity.class, entity, oldTree));
  }

  @Test
  public void updateDomainEntityWithOtherVariant() throws Exception {
    // tree to be updated
    Map<String, Object> oldMap = newDomainEntityMap(ID, PID);
    oldMap.put(propertyName(BaseDomainEntity.class, "value1"), "v1");
    oldMap.put(propertyName(BaseDomainEntity.class, "value2"), "v2");
    oldMap.put(propertyName(SubADomainEntity.class, "value1"), "v1");
    oldMap.put(propertyName(SubADomainEntity.class, "value2"), "v2");
    oldMap.put(propertyName(SubADomainEntity.class, "valuea"), "va");
    ObjectNode oldTree = mapper.valueToTree(oldMap);

    // entity to update with
    SubBDomainEntity entity = new SubBDomainEntity(ID, PID);
    entity.setValue1("x1");
    entity.setValue2(null);
    entity.setValueb("xb");

    // expected tree after update
    Map<String, Object> newMap = newDomainEntityMap(ID, PID);
    newMap.put(propertyName(BaseDomainEntity.class, "value1"), "v1");
    newMap.put(propertyName(BaseDomainEntity.class, "value2"), "v2");
    newMap.put(propertyName(SubADomainEntity.class, "value1"), "v1");
    newMap.put(propertyName(SubADomainEntity.class, "value2"), "v2");
    newMap.put(propertyName(SubADomainEntity.class, "valuea"), "va");
    newMap.put(propertyName(SubBDomainEntity.class, "value1"), "x1");
    newMap.put(propertyName(SubBDomainEntity.class, "valueb"), "xb");
    ObjectNode newTree = mapper.valueToTree(newMap);

    assertEquals(newTree, inducer.convertDomainEntityForUpdate(SubBDomainEntity.class, entity, oldTree));
  }

  // --- specific cases ------------------------------------------------

  @Test
  // [#1781] Test that prefix '^' works for complex properties
  public void induceDomainEntityWithReferences() throws Exception {
    Reference sharedReference = new Reference("type1", "id1");
    Reference uniqueReference = new Reference("type2", "id2");

    DomainEntityWithReferences entity = new DomainEntityWithReferences(ID);
    entity.setSharedReference(sharedReference);
    entity.setUniqueReference(uniqueReference);

    Map<String, Object> map = newDomainEntityMap(ID, null);
    map.put(propertyName(DomainEntityWithReferences.class, "sharedReference"), sharedReference);
    map.put("^uniqueReference", uniqueReference);
    JsonNode expected = mapper.valueToTree(map);

    assertEquals(expected, inducer.convertDomainEntityForAdd(DomainEntityWithReferences.class, entity));
  }

}
