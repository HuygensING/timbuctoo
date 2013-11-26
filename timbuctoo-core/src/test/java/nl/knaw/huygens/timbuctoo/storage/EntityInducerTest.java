package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projectb.ProjectBDomainEntity;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

public class EntityInducerTest {

  private final static String SYSTEM_ID = "TSTD000000000042";

  private static final String DEFAULT_PID = "test pid";
  private static final String DOMAIN_ID = "GTD0000000012";
  private static final String TEST_NAME = "test";

  private static TypeRegistry registry;

  private EntityInducer inducer;
  protected ObjectMapper mapper;

  @BeforeClass
  public static void setupRegistry() {
    registry = new TypeRegistry("timbuctoo.variation.model timbuctoo.variation.model.projecta timbuctoo.variation.model.projectb timbuctoo.model");
  }

  @Before
  public void setup() throws Exception {
    inducer = new EntityInducer(registry);
    mapper = new ObjectMapper();
  }

  protected void addNonNullValue(Map<String, Object> map, String key, String value) {
    if (value != null) {
      map.put(key, value);
    }
  }

  protected ObjectNode newSystemEntityNode(String id, String name, String testValue1, String testValue2) {
    Map<String, Object> map = Maps.newHashMap();
    addNonNullValue(map, "_id", id);
    addNonNullValue(map, propertyName(TestSystemEntity.class, "name"), name);
    addNonNullValue(map, propertyName(TestSystemEntity.class, "testValue1"), testValue1);
    addNonNullValue(map, propertyName(TestSystemEntity.class, "testValue2"), testValue2);
    map.put("^rev", 0);
    return mapper.valueToTree(map);
  }

  protected Map<String, Object> newDomainEntityMap(String id, String pid) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("_id", id);
    map.put("^rev", 0);
    map.put(DomainEntity.PID, pid);
    map.put(DomainEntity.DELETED, false);
    return map;
  }

  protected Map<String, Object> newBaseDomainEntityMap(String id, String pid, String value) {
    Map<String, Object> map = newDomainEntityMap(id, pid);
    addNonNullValue(map, propertyName(BaseDomainEntity.class, "name"), value);
    return map;
  }

  protected ProjectBDomainEntity newProjectBDomainEntity(String id, String pid, String projectBGeneralTestDocValue) {
    ProjectBDomainEntity entity = new ProjectBDomainEntity(id);
    entity.projectBGeneralTestDocValue = projectBGeneralTestDocValue;
    entity.setPid(pid);
    return entity;
  }

  // --- new system entity ---------------------------------------------

  @Test
  public void induceSystemEntityAsPrimitive() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(SYSTEM_ID, TEST_NAME);
    entity.setTestValue1("value1");
    entity.setTestValue2("value2");

    System.out.println(inducer.induceNewEntity(TestSystemEntity.class, entity));

    JsonNode expected = newSystemEntityNode(SYSTEM_ID, TEST_NAME, "value1", "value2");

    assertEquals(expected, inducer.induceNewEntity(TestSystemEntity.class, entity));
  }

  @Test
  public void induceSystemEntityAsSystemEntity() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(SYSTEM_ID, TEST_NAME);
    entity.setTestValue1("value1");
    entity.setTestValue2("value2");

    System.out.println(inducer.induceNewEntity(SystemEntity.class, entity));

    JsonNode expected = newSystemEntityNode(SYSTEM_ID, null, null, null);

    assertEquals(expected, inducer.induceNewEntity(SystemEntity.class, entity));
  }

  @Test(expected = IllegalArgumentException.class)
  public void induceSystemEntityAsEntity() throws Exception {
    TestSystemEntity entity = new TestSystemEntity(SYSTEM_ID, TEST_NAME);
    entity.setTestValue1("value1");
    entity.setTestValue2("value2");

    inducer.induceNewEntity(Entity.class, entity);
  }

  // --- new primitive domain entitiy ----------------------------------

  @Test
  public void inducePrimitiveDomainEntityAsPrimitive() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(DOMAIN_ID, "test1");
    entity.setPid(DEFAULT_PID);

    System.out.println(inducer.induceNewEntity(BaseDomainEntity.class, entity));

    Map<String, Object> map = newBaseDomainEntityMap(DOMAIN_ID, DEFAULT_PID, "test1");
    JsonNode expected = mapper.valueToTree(map);

    assertEquals(expected, inducer.induceNewEntity(BaseDomainEntity.class, entity));
  }

  @Test
  public void inducePrimitiveDomainEntityAsDomainEntity() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(DOMAIN_ID, "test1");
    entity.setPid(DEFAULT_PID);

    System.out.println(inducer.induceNewEntity(DomainEntity.class, entity));

    Map<String, Object> map = newBaseDomainEntityMap(DOMAIN_ID, DEFAULT_PID, null);
    JsonNode expected = mapper.valueToTree(map);

    assertEquals(expected, inducer.induceNewEntity(DomainEntity.class, entity));
  }

  @Test(expected = IllegalArgumentException.class)
  public void inducePrimitiveDomainEntityAsEntity() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity(DOMAIN_ID, "test1");
    entity.setPid(DEFAULT_PID);

    inducer.induceNewEntity(Entity.class, entity);
  }

}
