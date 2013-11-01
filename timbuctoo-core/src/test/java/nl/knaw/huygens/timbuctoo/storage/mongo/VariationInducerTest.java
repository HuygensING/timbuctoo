package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

public class VariationInducerTest {

  private static final String TEST_NAME = "test";
  private final static String TEST_SYSTEM_ID = "TSD";
  private static TypeRegistry registry;

  private ObjectMapper mapper;
  private VariationInducer inducer;

  @BeforeClass
  public static void setupRegistry() {
    registry = new TypeRegistry("timbuctoo.variation.model timbuctoo.variation.model.projecta timbuctoo.variation.model.projectb, timbuctoo.model");
  }

  @Before
  public void setUp() throws Exception {
    mapper = new ObjectMapper();
    inducer = new VariationInducer(registry, JsonViews.DBView.class);
  }

  @After
  public void tearDown() {
    mapper = null;
    inducer = null;
  }

  @Test
  public void induceSystemEntity() throws JsonProcessingException, IOException {
    String testValue1 = "value";
    String testValue2 = "testValue2";

    JsonNode expected = createSystemObjectNode(TEST_SYSTEM_ID, TEST_NAME, testValue1, testValue2);

    TestSystemEntity doc = new TestSystemEntity();
    doc.setId(TEST_SYSTEM_ID);
    doc.setName(TEST_NAME);
    doc.setTestValue1(testValue1);
    doc.setTestValue2(testValue2);

    JsonNode actual = inducer.induce(TestSystemEntity.class, doc);

    assertEquals(expected, actual);
  }

  @Test
  public void induceSystemEntityWithNullValues() throws JsonProcessingException, IOException {
    String testValue1 = "value";

    JsonNode expected = createSystemObjectNode(TEST_SYSTEM_ID, null, testValue1, null);

    TestSystemEntity doc = new TestSystemEntity();
    doc.setId(TEST_SYSTEM_ID);
    doc.setTestValue1(testValue1);

    JsonNode actual = inducer.induce(TestSystemEntity.class, doc);

    assertEquals(expected, actual);
  }

  @Test
  public void induceUpdatedSystemEntity() throws JsonProcessingException, IOException {
    String testValue1 = "value";
    String testValue2 = "testValue2";
    String name2 = "name2";

    JsonNode expectedObject = createSystemObjectNode(TEST_SYSTEM_ID, name2, testValue1, testValue2);
    ObjectNode existingObject = createSystemObjectNode(TEST_SYSTEM_ID, TEST_NAME, testValue1, null);

    TestSystemEntity doc = new TestSystemEntity();
    doc.setId(TEST_SYSTEM_ID);
    doc.setName(name2);
    doc.setTestValue1(testValue1);
    doc.setTestValue2(testValue2);

    JsonNode actual = inducer.induce(TestSystemEntity.class, doc, existingObject);

    assertEquals(expectedObject, actual);
  }

  protected ObjectNode createSystemObjectNode(String id, String name, String testValue1, String testValue2) throws IOException, JsonProcessingException {
    Map<String, Object> map = Maps.newHashMap();
    addNonNullValueToMap(map, "_id", id);
    addNonNullValueToMap(map, "testsystementity.name", name);
    addNonNullValueToMap(map, "testsystementity.testValue1", testValue1);
    addNonNullValueToMap(map, "testsystementity.testValue2", testValue2);
    map.put("^rev", 0);
    map.put("_deleted", false);

    return (ObjectNode) mapper.valueToTree(map);
  }

  private void addNonNullValueToMap(Map<String, Object> map, String key, String value) {
    if (value != null) {
      map.put(key, value);
    }
  }

  @Ignore
  @Test
  public void induceDomainEntity() {
    fail("Yet to be implemented.");
  }

  @Ignore
  @Test
  public void induceUpdatedDomainEntity() {
    fail("Yet to be implemented.");
  }

  @Ignore
  @Test
  public void induceDomainEntityNewVariation() {
    fail("Yet to be implemented.");
  }

  @Ignore
  @Test
  public void induceDomainEntityWithRole() {
    fail("Yet to be implemented.");
  }

  @Ignore
  @Test
  public void induceUpdatedDomainEntityWithRole() {
    fail("Yet to be implemented.");
  }

  @Ignore
  @Test
  public void induceUpdatedDomainEntityWithRolesNewVariation() {
    fail("Yet to be implemented.");
  }

  @Ignore
  @Test
  public void induceUpdatedDomainEntityWithRolesNewRole() {
    fail("Yet to be implemented.");
  }

  @Ignore
  @Test
  public void testInduceNullEntity() {
    fail("Yet to be implemented.");
  }

  @Ignore
  @Test
  public void testInduceNullType() {
    fail("Yet to be implemented.");
  }
}
