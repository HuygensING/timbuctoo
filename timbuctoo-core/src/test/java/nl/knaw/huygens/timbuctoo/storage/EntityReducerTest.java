package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.model.BaseDomainEntity;
import test.model.TestSystemEntity;
import test.model.projecta.SubADomainEntity;
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

  private JsonNode newTestSystemEntityTree() {
    Map<String, Object> map = Maps.newHashMap();
    map.put("_id", ID);
    map.put("^rev", 0);
    map.put(propertyName(TestSystemEntity.class, "value1"), "v1");
    map.put(propertyName(TestSystemEntity.class, "value2"), "v2");
    return mapper.valueToTree(map);
  }

  private JsonNode newTestDomainEntityTree() {
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
    return mapper.valueToTree(map);
  }

  // -------------------------------------------------------------------

  @Test
  public void testReduceSystemEntity() throws Exception {
    JsonNode tree = newTestSystemEntityTree();

    TestSystemEntity entity = reducer.reduceVariation(TestSystemEntity.class, tree);
    assertEquals(ID, entity.getId());
    assertEquals("v1", entity.getValue1());
    assertEquals("v2", entity.getValue2());
    assertEquals(0, entity.getRev());
  }

  @Test
  public void testReduceVariationPrimitive() throws Exception {
    JsonNode tree = newTestDomainEntityTree();

    BaseDomainEntity entity = reducer.reduceVariation(BaseDomainEntity.class, tree);
    assertEquals(ID, entity.getId());
    assertEquals("v1", entity.getValue1());
    assertEquals("v2", entity.getValue2());
    assertEquals(0, entity.getRev());
  }

  @Test
  public void testReduceVariationDerived() throws Exception {
    JsonNode tree = newTestDomainEntityTree();

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
    JsonNode tree = newTestDomainEntityTree();

    List<BaseDomainEntity> entities = reducer.reduceAllVariations(BaseDomainEntity.class, tree);
    assertEquals(3, entities.size());
    assertEquals(BaseDomainEntity.class, entities.get(0).getClass());
    assertEquals(SubADomainEntity.class, entities.get(1).getClass());
    assertEquals(SubBDomainEntity.class, entities.get(2).getClass());
  }

  @Test
  public void testReduceAllVariations2() throws Exception {
    JsonNode tree = newTestDomainEntityTree();

    List<SubADomainEntity> entities = reducer.reduceAllVariations(SubADomainEntity.class, tree);
    assertEquals(1, entities.size());
    assertEquals(SubADomainEntity.class, entities.get(0).getClass());
  }

}
