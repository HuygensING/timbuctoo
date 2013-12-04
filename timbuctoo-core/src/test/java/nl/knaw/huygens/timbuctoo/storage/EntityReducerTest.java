package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Role;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.model.BaseDomainEntity;
import test.model.TestRole;
import test.model.TestSystemEntity;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.TestRoleA1;
import test.model.projecta.TestRoleA2;
import test.model.projectb.SubBDomainEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

public class EntityReducerTest {

  private final static String ID = "TEST042";

  private static TypeRegistry registry;

  private EntityReducer reducer;
  private ObjectMapper mapper;

  @BeforeClass
  public static void setupRegistry() {
    registry = new TypeRegistry("test.model test.model.projecta test.model.projectb");
  }

  @Before
  public void setup() throws Exception {
    reducer = new EntityReducer(registry);
    mapper = new ObjectMapper();
  }

  private JsonNode newSystemEntityTree() {
    Map<String, Object> map = Maps.newHashMap();
    map.put("_id", ID);
    map.put("^rev", 0);
    map.put(propertyName(TestSystemEntity.class, "value1"), "v1");
    map.put(propertyName(TestSystemEntity.class, "value2"), "v2");
    return mapper.valueToTree(map);
  }

  private Map<String, Object> newDomainEntityMap() {
    Map<String, Object> map = Maps.newHashMap();
    map.put("_id", ID);
    map.put("^rev", 0);
    map.put("^variations", new String[] { "basedomainentity", "subadomainentity", "subbdomainentity" });
    map.put(propertyName(BaseDomainEntity.class, "value1"), "v1");
    map.put(propertyName(BaseDomainEntity.class, "value2"), "v2");
    map.put(propertyName(SubADomainEntity.class, "value1"), "v1");
    map.put(propertyName(SubADomainEntity.class, "value2"), "v2");
    map.put(propertyName(SubADomainEntity.class, "valuea"), "va");
    map.put(propertyName(SubBDomainEntity.class, "value1"), "v1");
    map.put(propertyName(SubBDomainEntity.class, "value2"), "v2");
    map.put(propertyName(SubBDomainEntity.class, "valueb"), "vb");
    return map;
  }

  private JsonNode newDomainEntityTree() {
    Map<String, Object> map = newDomainEntityMap();
    return mapper.valueToTree(map);
  }

  private JsonNode newDomainEntityWithRolesTree() {
    Map<String, Object> map = newDomainEntityMap();
    map.put(propertyName(TestRole.class, "property"), "p");
    map.put(propertyName(TestRoleA1.class, "property"), "p");
    map.put(propertyName(TestRoleA1.class, "propertyA1"), "pA1");
    map.put(propertyName(TestRoleA2.class, "property"), "p");
    map.put(propertyName(TestRoleA2.class, "propertyA2"), "pA2");
    return mapper.valueToTree(map);
  }

  // -------------------------------------------------------------------

  @Test
  public void testReduceSystemEntity() throws Exception {
    JsonNode tree = newSystemEntityTree();

    TestSystemEntity entity = reducer.reduceVariation(TestSystemEntity.class, tree);
    assertEquals(ID, entity.getId());
    assertEquals("v1", entity.getValue1());
    assertEquals("v2", entity.getValue2());
    assertEquals(0, entity.getRev());
  }

  @Test
  public void testReduceVariationPrimitive() throws Exception {
    JsonNode tree = newDomainEntityTree();

    BaseDomainEntity entity = reducer.reduceVariation(BaseDomainEntity.class, tree);
    assertEquals(ID, entity.getId());
    assertEquals("v1", entity.getValue1());
    assertEquals("v2", entity.getValue2());
    assertEquals(0, entity.getRev());
  }

  @Test
  public void testReduceVariationDerived() throws Exception {
    JsonNode tree = newDomainEntityTree();

    SubADomainEntity entity = reducer.reduceVariation(SubADomainEntity.class, tree);
    assertEquals(ID, entity.getId());
    assertEquals("v1", entity.getValue1());
    assertEquals("v2", entity.getValue2());
    assertEquals("va", entity.getValuea());
    assertEquals(0, entity.getRev());
  }

  @Test
  // A similar test failed with old reducer [#1919]
  public void testReduceAllVariations() throws Exception {
    JsonNode tree = newDomainEntityTree();

    List<BaseDomainEntity> entities = reducer.reduceAllVariations(BaseDomainEntity.class, tree);
    assertEquals(3, entities.size());
    assertEquals(BaseDomainEntity.class, entities.get(0).getClass());
    assertEquals(SubADomainEntity.class, entities.get(1).getClass());
    assertEquals(SubBDomainEntity.class, entities.get(2).getClass());
  }

  @Test
  public void testReduceAllVariations2() throws Exception {
    JsonNode tree = newDomainEntityTree();

    List<SubADomainEntity> entities = reducer.reduceAllVariations(SubADomainEntity.class, tree);
    assertEquals(1, entities.size());
    assertEquals(SubADomainEntity.class, entities.get(0).getClass());
  }

  @Test
  public void reducePrimitiveDomainEntityWithRoles() throws Exception {
    JsonNode tree = newDomainEntityWithRolesTree();

    BaseDomainEntity entity = reducer.reduceVariation(BaseDomainEntity.class, tree);
    List<Role> roles = entity.getRoles();
    assertEquals(1, roles.size());
    assertEquals(TestRole.class, roles.get(0).getClass());
    TestRole role = TestRole.class.cast(roles.get(0));
    assertEquals("p", role.getProperty());
  }

  @Test
  public void reduceDerivedDomainEntityWithRoles() throws Exception {
    JsonNode tree = newDomainEntityWithRolesTree();

    SubADomainEntity entity = reducer.reduceVariation(SubADomainEntity.class, tree);
    List<Role> roles = entity.getRoles();
    assertEquals(2, roles.size());
    // roles need not be sorted...
    int indexA1 = (roles.get(0).getClass() == TestRoleA1.class) ? 0 : 1;
    TestRoleA1 roleA1 = TestRoleA1.class.cast(roles.get(indexA1));
    assertEquals("p", roleA1.getProperty());
    assertEquals("pA1", roleA1.getPropertyA1());
    TestRoleA2 roleA2 = TestRoleA2.class.cast(roles.get(1 - indexA1));
    assertEquals("p", roleA2.getProperty());
    assertEquals("pA2", roleA2.getPropertyA2());
  }

}
