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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

public class EntityInducerTest {

  private final static String SYSTEM_ID = "TSYS000000000042";
  private static final String DOMAIN_ID = "TDOM000000000007";
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

  private ObjectNode newBaseDomainEntityTree(String id, String pid, String value1, String value2) {
    Map<String, Object> map = newDomainEntityMap(id, pid);
    addValue(map, propertyName(BaseDomainEntity.class, "value1"), value1);
    addValue(map, propertyName(BaseDomainEntity.class, "value2"), value2);
    return mapper.valueToTree(map);
  }

  // --- new system entity ---------------------------------------------

  @Test
  public void induceSystemEntityAsPrimitive() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(SYSTEM_ID, "v1", "v2");

    JsonNode expected = newSystemEntityTree(SYSTEM_ID, "v1", "v2");

    assertEquals(expected, inducer.induceNewEntity(TestSystemEntity.class, entity));
  }

  @Test
  public void induceSystemEntityAsSystemEntity() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(SYSTEM_ID, "v1", "v2");

    JsonNode expected = newSystemEntityTree(SYSTEM_ID, null, null);

    assertEquals(expected, inducer.induceNewEntity(SystemEntity.class, entity));
  }

  @Test(expected = IllegalArgumentException.class)
  public void induceSystemEntityAsEntity() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(SYSTEM_ID, "v1", "v2");

    inducer.induceNewEntity(Entity.class, entity);
  }

  // --- new primitive domain entitiy ----------------------------------

  @Test
  public void inducePrimitiveDomainEntityAsPrimitive() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(DOMAIN_ID, PID, "v1", "v2");

    JsonNode expected = newBaseDomainEntityTree(DOMAIN_ID, PID, "v1", "v2");

    assertEquals(expected, inducer.induceNewEntity(BaseDomainEntity.class, entity));
  }

  @Test
  public void inducePrimitiveDomainEntityAsDomainEntity() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(DOMAIN_ID, PID, "v1", "v2");

    JsonNode expected = newBaseDomainEntityTree(DOMAIN_ID, PID, null, null);

    assertEquals(expected, inducer.induceNewEntity(DomainEntity.class, entity));
  }

  @Test(expected = IllegalArgumentException.class)
  public void inducePrimitiveDomainEntityAsEntity() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(DOMAIN_ID, PID, "v1", "v2");

    inducer.induceNewEntity(Entity.class, entity);
  }

}
