package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.junit.Before;
import org.junit.Test;

import test.model.BaseDomainEntity;
import test.model.TestRole;
import test.model.TestSystemEntity;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.TestRole1;
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
    inducer = new EntityInducer();
    mapper = new ObjectMapper();
  }

  private void addValue(Map<String, Object> map, String key, String value) {
    if (value != null) {
      map.put(key, value);
    }
  }

  private ObjectNode newSystemEntityTree(String id, String value1, String value2) {
    Map<String, Object> map = Maps.newTreeMap();
    addValue(map, "_id", id);
    map.put("^rev", 0);
    addValue(map, propertyName(TestSystemEntity.class, "value1"), value1);
    addValue(map, propertyName(TestSystemEntity.class, "value2"), value2);
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
    TestSystemEntity entity = new TestSystemEntity(ID, "v1", "v2");
    JsonNode expected = newSystemEntityTree(ID, "v1", "v2");
    assertEquals(expected, inducer.induceSystemEntity(TestSystemEntity.class, entity));
  }

  @Test(expected = IllegalArgumentException.class)
  public void induceSystemEntityAsSystemEntity() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(ID, "v1", "v2");
    inducer.induceSystemEntity(SystemEntity.class, entity);
  }

  // --- new primitive domain entitiy ----------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void inducePrimitiveDomainEntityAsPrimitive() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(ID, PID, "v1", "v2");
    inducer.induceDomainEntity(BaseDomainEntity.class, entity);
  }

  @Test(expected = IllegalArgumentException.class)
  public void inducePrimitiveDomainEntityAsDomainEntity() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(ID, PID, "v1", "v2");
    inducer.induceDomainEntity(DomainEntity.class, entity);
  }

  // --- new project domain entitiy ------------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void induceDerivedDomainEntityAsPrimitive() throws Exception {
    SubADomainEntity entity = new SubADomainEntity(ID, PID, "v1", "v2", "va");
    inducer.induceDomainEntity(BaseDomainEntity.class, entity);
  }

  @Test
  public void induceDerivedDomainEntityAsDerived() throws Exception {
    SubADomainEntity entity = new SubADomainEntity(ID, PID, "v1", "v2", "va");
    JsonNode expected = newSubADomainEntityTree(ID, PID, "v1", "v2", "v1", "v2", "va");
    assertEquals(expected, inducer.induceDomainEntity(SubADomainEntity.class, entity));
  }

  @Test
  public void induceDerivedDomainEntityWithRoles() throws Exception {
    SubADomainEntity entity = new SubADomainEntity(ID, PID, "v1", "v2", "va");
    entity.addRole(new TestRole1("p", "p1"));

    Map<String, Object> map = newSubADomainEntityMap(ID, PID, "v1", "v2", "v1", "v2", "va");
    map.put(propertyName(TestRole.class, "property"), "p");
    map.put(propertyName(TestRole1.class, "property"), "p");
    map.put(propertyName(TestRole1.class, "property1"), "p1");
    JsonNode expected = mapper.valueToTree(map);

    assertEquals(expected, inducer.induceDomainEntity(SubADomainEntity.class, entity));
  }

  // --- old system entity ---------------------------------------------

  @Test
  public void updateSystemEntityWithPrimitiveView() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(ID, "updated", null);
    ObjectNode oldTree = newSystemEntityTree(ID, "v1", "v2");
    ObjectNode newTree = newSystemEntityTree(ID, "updated", null);
    assertEquals(newTree, inducer.induceSystemEntity(TestSystemEntity.class, entity, oldTree));
  }

  @Test
  public void updateSystemEntityWithWrongView() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(ID, "updated", null);
    ObjectNode oldTree = newSystemEntityTree(ID, "v1", "v2");
    ObjectNode newTree = newSystemEntityTree(ID, "v1", "v2");
    assertEquals(newTree, inducer.induceSystemEntity(SystemEntity.class, entity, oldTree));
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

    assertEquals(newTree, inducer.induceDomainEntity(SubADomainEntity.class, entity, oldTree));
  }

  @Test
  // A similar test failed with old inducer [#1909]
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

    assertEquals(newTree, inducer.induceDomainEntity(BaseDomainEntity.class, entity, oldTree));
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

    assertEquals(newTree, inducer.induceDomainEntity(DomainEntity.class, entity, oldTree));
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

    assertEquals(newTree, inducer.induceDomainEntity(SubBDomainEntity.class, entity, oldTree));
  }

  @Test
  public void updateOfProjectWithRoles() throws Exception {
    // tree to be updated
    Map<String, Object> oldMap = newDomainEntityMap(ID, PID);
    oldMap.put(propertyName(BaseDomainEntity.class, "value1"), "v1");
    oldMap.put(propertyName(BaseDomainEntity.class, "value2"), "v2");
    oldMap.put(propertyName(SubADomainEntity.class, "value1"), "v1");
    oldMap.put(propertyName(SubADomainEntity.class, "value2"), "v2");
    oldMap.put(propertyName(SubADomainEntity.class, "valuea"), "va");
    oldMap.put(propertyName(TestRole.class, "property"), "p");
    oldMap.put(propertyName(TestRole1.class, "property"), "p");
    oldMap.put(propertyName(TestRole1.class, "property1"), "p1");
    ObjectNode oldTree = mapper.valueToTree(oldMap);

    // entity to update with
    SubADomainEntity entity = new SubADomainEntity(ID, PID);
    entity.setValue1("xv1");
    entity.setValue2(null);
    entity.setValuea("xva");
    entity.addRole(new TestRole1("px", "px1"));

    // expected tree after update
    Map<String, Object> newMap = newDomainEntityMap(ID, PID);
    newMap.put(propertyName(BaseDomainEntity.class, "value1"), "v1");
    newMap.put(propertyName(BaseDomainEntity.class, "value2"), "v2");
    newMap.put(propertyName(SubADomainEntity.class, "value1"), "xv1");
    newMap.put(propertyName(SubADomainEntity.class, "valuea"), "xva");
    newMap.put(propertyName(TestRole.class, "property"), "p");
    newMap.put(propertyName(TestRole1.class, "property"), "px");
    newMap.put(propertyName(TestRole1.class, "property1"), "px1");
    ObjectNode newTree = mapper.valueToTree(newMap);

    assertEquals(newTree, inducer.induceDomainEntity(SubADomainEntity.class, entity, oldTree));
  }

}
