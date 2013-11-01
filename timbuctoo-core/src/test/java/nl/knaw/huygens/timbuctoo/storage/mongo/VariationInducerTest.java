package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projectb.ProjectBGeneralTestDoc;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class VariationInducerTest {

  private static final String DEFAULT_DOMAIN_ID = "GTD0000000012";
  private static final String TEST_NAME = "test";
  private final static String TEST_SYSTEM_ID = "TSD";
  private static TypeRegistry registry;
  private static MongoObjectMapper mongoMapper;

  private ObjectMapper mapper;
  private VariationInducer inducer;

  @BeforeClass
  public static void setupMapper() {
    registry = new TypeRegistry("timbuctoo.variation.model timbuctoo.variation.model.projecta timbuctoo.variation.model.projectb timbuctoo.model");
    mongoMapper = new MongoObjectMapper(registry);
  }

  @Before
  public void setUp() throws Exception {
    mapper = new ObjectMapper();
    inducer = new VariationInducer(registry, JsonViews.DBView.class, mongoMapper);
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

    return mapper.valueToTree(map);
  }

  private void addNonNullValueToMap(Map<String, Object> map, String key, String value) {
    if (value != null) {
      map.put(key, value);
    }
  }

  @Ignore("Should we be able to induce primitive (from the model package) entities?")
  @Test
  public void induceDomainEntityPrimitive() {
    fail("Yet to be implemented.");
  }

  @Ignore("Should we be able to induce primitive (from the model package) entities?")
  @Test
  public void induceUpdatedDomainEntityPrimitive() {
    fail("Yet to be implemented.");
  }

  @Test
  public void induceDomainEntityProject() throws VariationException {
    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setCurrentVariation("projecta");
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid("test pid");
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "test";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    expectedMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");

    JsonNode expected = mapper.valueToTree(expectedMap);

    JsonNode actual = inducer.induce(ProjectAGeneralTestDoc.class, item);

    assertEquals(expected, actual);
  }

  protected Map<String, Object> createGeneralTestDocMap(String id, String pid, String generalTestDocValue) {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("_id", id);
    expectedMap.put("^rev", 0);
    expectedMap.put("_deleted", false);
    expectedMap.put("^pid", pid);
    expectedMap.put("generaltestdoc.generalTestDocValue", generalTestDocValue);

    return expectedMap;
  }

  @Test
  public void testInduceDomainEntityNewVariationAddValue() throws VariationException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    existingMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    ObjectNode existing = mapper.valueToTree(existingMap);

    ProjectBGeneralTestDoc item = new ProjectBGeneralTestDoc();
    item.projectBGeneralTestDocValue = "testB";
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID), new Reference(ProjectBGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setCurrentVariation("projectb");
    item.setPid("test pid");
    item.setId(DEFAULT_DOMAIN_ID);

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    expectedMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    expectedMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");

    JsonNode expected = mapper.valueToTree(expectedMap);

    JsonNode actual = inducer.induce(ProjectBGeneralTestDoc.class, item, existing);

    assertEquals(expected, actual);

  }

  @Test
  public void testInduceDomainEntityNewVariationExistingValue() throws VariationException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    existingMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    ObjectNode existing = mapper.valueToTree(existingMap);

    ProjectBGeneralTestDoc item = new ProjectBGeneralTestDoc();
    item.projectBGeneralTestDocValue = "testB";
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID), new Reference(ProjectBGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setCurrentVariation("projectb");
    item.setPid("test pid");
    item.setId(DEFAULT_DOMAIN_ID);
    item.generalTestDocValue = "projectbTestDoc";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    expectedMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    expectedMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    expectedMap.put("projectbgeneraltestdoc.generalTestDocValue", "projectbTestDoc");

    JsonNode expected = mapper.valueToTree(expectedMap);

    JsonNode actual = inducer.induce(ProjectBGeneralTestDoc.class, item, existing);

    assertEquals(expected, actual);
  }

  @Test
  public void induceDomainEntityWithRole() {
    fail("Yet to be implemented.");
  }

  @Test
  public void induceUpdatedDomainEntityWithRole() {
    fail("Yet to be implemented.");
  }

  @Test
  public void induceUpdatedDomainEntityWithRolesNewVariation() {
    fail("Yet to be implemented.");
  }

  @Test
  public void induceUpdatedDomainEntityWithRolesNewRole() {
    fail("Yet to be implemented.");
  }

  @Test
  public void testInduceNullEntity() {
    fail("Yet to be implemented.");
  }

  @Test
  public void testInduceNullType() {
    fail("Yet to be implemented.");
  }
}
