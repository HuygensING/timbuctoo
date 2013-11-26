package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntity;
import test.model.projecta.SubADomainEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

public class EntityInducerTest {

  private final static String ID = "TEST042";
  private static final String PID = "test_pid";

  private static TypeRegistry registry;

  private EntityInducer inducer;
  private ObjectMapper mapper;

  @BeforeClass
  public static void setupRegistry() {
    registry = new TypeRegistry("test.model test.model.projecta");
  }

  @Before
  public void setup() throws Exception {
    inducer = new EntityInducer(registry);
    mapper = new ObjectMapper();
  }

  private void addToMap(Map<String, Object> map, String key, String value) {
    if (value != null) {
      map.put(key, value);
    }
  }

  private ObjectNode newSystemEntityTree(String id, String value1, String value2) {
    Map<String, Object> map = Maps.newTreeMap();
    addToMap(map, "_id", id);
    addToMap(map, propertyName(TestSystemEntity.class, "value1"), value1);
    addToMap(map, propertyName(TestSystemEntity.class, "value2"), value2);
    map.put("^rev", 0);
    return mapper.valueToTree(map);
  }

  private Map<String, Object> newDomainEntityMap(String id, String pid) {
    Map<String, Object> map = Maps.newTreeMap();
    addToMap(map, "_id", id);
    addToMap(map, DomainEntity.PID, pid);
    map.put(DomainEntity.DELETED, false);
    map.put("^rev", 0);
    return map;
  }

  private ObjectNode newBaseDomainEntityTree(String id, String pid, String value1, String value2) {
    Map<String, Object> map = newDomainEntityMap(id, pid);
    addToMap(map, propertyName(BaseDomainEntity.class, "value1"), value1);
    addToMap(map, propertyName(BaseDomainEntity.class, "value2"), value2);
    return mapper.valueToTree(map);
  }

  private ObjectNode newSubADomainEntityTree(String id, String pid, String value1, String value2, String valuea) {
    Map<String, Object> map = newDomainEntityMap(id, pid);
    addToMap(map, propertyName(SubADomainEntity.class, "value1"), value1);
    addToMap(map, propertyName(SubADomainEntity.class, "value2"), value2);
    addToMap(map, propertyName(SubADomainEntity.class, "valuea"), valuea);
    return mapper.valueToTree(map);
  }

  // --- new system entity ---------------------------------------------

  @Test
  public void induceSystemEntityAsPrimitive() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(ID, "v1", "v2");
    JsonNode expected = newSystemEntityTree(ID, "v1", "v2");
    assertEquals(expected, inducer.induceNewSystemEntity(TestSystemEntity.class, entity));
  }

  @Test
  public void induceSystemEntityAsSystemEntity() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(ID, "v1", "v2");
    JsonNode expected = newSystemEntityTree(ID, null, null);
    assertEquals(expected, inducer.induceNewSystemEntity(SystemEntity.class, entity));
  }

  @Test(expected = IllegalArgumentException.class)
  public void induceSystemEntityAsEntity() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(ID, "v1", "v2");
    inducer.induceNewSystemEntity(Entity.class, entity);
  }

  // --- new primitive domain entitiy ----------------------------------

  @Test
  public void inducePrimitiveDomainEntityAsPrimitive() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(ID, PID, "v1", "v2");
    JsonNode expected = newBaseDomainEntityTree(ID, PID, "v1", "v2");
    assertEquals(expected, inducer.induceNewDomainEntity(BaseDomainEntity.class, entity));
  }

  @Test
  public void inducePrimitiveDomainEntityAsDomainEntity() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(ID, PID, "v1", "v2");
    JsonNode expected = newBaseDomainEntityTree(ID, PID, null, null);
    assertEquals(expected, inducer.induceNewDomainEntity(DomainEntity.class, entity));
  }

  @Test(expected = IllegalArgumentException.class)
  public void inducePrimitiveDomainEntityAsEntity() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(ID, PID, "v1", "v2");
    inducer.induceNewDomainEntity(Entity.class, entity);
  }

  // --- new project domain entitiy ------------------------------------

  @Test
  public void induceDerivedDomainEntityAsDerived() throws Exception {
    SubADomainEntity entity = new SubADomainEntity(ID, PID, "v1", "v2", "va");
    JsonNode expected = newSubADomainEntityTree(ID, PID, "v1", "v2", "va");
    assertEquals(expected, inducer.induceNewDomainEntity(SubADomainEntity.class, entity));
  }

  @Test
  public void induceDerivedDomainEntityAsPrimitive() throws Exception {
    SubADomainEntity entity = new SubADomainEntity(ID, PID, "v1", "v2", "va");
    JsonNode expected = newBaseDomainEntityTree(ID, PID, "v1", "v2");
    assertEquals(expected, inducer.induceNewDomainEntity(BaseDomainEntity.class, entity));
  }

  @Test
  public void induceDerivedDomainEntityAsDomainEntity() throws Exception {
    SubADomainEntity entity = new SubADomainEntity(ID, PID, "v1", "v2", "va");
    JsonNode expected = newBaseDomainEntityTree(ID, PID, null, null);
    assertEquals(expected, inducer.induceNewDomainEntity(DomainEntity.class, entity));
  }

  @Test(expected = IllegalArgumentException.class)
  public void induceDerivedDomainEntityAsEntity() throws Exception {
    SubADomainEntity entity = new SubADomainEntity(ID, PID, "v1", "v2", "va");
    inducer.induceNewDomainEntity(Entity.class, entity);
  }

}
