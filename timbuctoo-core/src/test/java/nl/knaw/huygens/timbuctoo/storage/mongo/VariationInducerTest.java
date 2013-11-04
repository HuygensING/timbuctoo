package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectANewTestRole;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectATestRole;
import nl.knaw.huygens.timbuctoo.variation.model.projectb.ProjectBGeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projectb.ProjectBTestRole;

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
    mongoMapper = new MongoObjectMapper();
  }

  @Before
  public void setUp() throws Exception {
    mapper = new ObjectMapper();
    inducer = new VariationInducer(registry, mongoMapper);
  }

  @After
  public void tearDown() {
    mapper = null;
    inducer = null;
  }

  @Test
  public void testInduceSystemEntity() throws JsonProcessingException, IOException {
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
  public void testInduceSystemEntityWithNullValues() throws JsonProcessingException, IOException {
    String testValue1 = "value";

    JsonNode expected = createSystemObjectNode(TEST_SYSTEM_ID, null, testValue1, null);

    TestSystemEntity doc = new TestSystemEntity();
    doc.setId(TEST_SYSTEM_ID);
    doc.setTestValue1(testValue1);

    JsonNode actual = inducer.induce(TestSystemEntity.class, doc);

    assertEquals(expected, actual);
  }

  @Test
  public void testInduceUpdatedSystemEntity() throws JsonProcessingException, IOException {
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
  public void testInduceDomainEntityPrimitive() {
    fail("Yet to be implemented.");
  }

  @Ignore("Should we be able to induce primitive (from the model package) entities?")
  @Test
  public void testInduceUpdatedDomainEntityPrimitive() {
    fail("Yet to be implemented.");
  }

  @Test
  public void testInduceDomainEntityProject() throws VariationException {
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

    ProjectBGeneralTestDoc item = createProjectBGeneralTestDoc();

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    expectedMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    expectedMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");

    JsonNode expected = mapper.valueToTree(expectedMap);

    JsonNode actual = inducer.induce(ProjectBGeneralTestDoc.class, item, existing);

    assertEquals(expected, actual);

  }

  protected ProjectBGeneralTestDoc createProjectBGeneralTestDoc() {
    ProjectBGeneralTestDoc item = new ProjectBGeneralTestDoc();
    item.projectBGeneralTestDocValue = "testB";
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID), new Reference(ProjectBGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setCurrentVariation("projectb");
    item.setPid("test pid");
    item.setId(DEFAULT_DOMAIN_ID);
    return item;
  }

  @Test
  public void testInduceDomainEntityNewVariationExistingValue() throws VariationException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    existingMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    ObjectNode existing = mapper.valueToTree(existingMap);

    ProjectBGeneralTestDoc item = createProjectBGeneralTestDoc();
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
  /*
   * Project value equals to the default value is updated.
   */
  public void testInduceDomainEntityVariationUpdated() throws VariationException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    existingMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    existingMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    existingMap.put("projectbgeneraltestdoc.generalTestDocValue", "projectbTestDoc");

    ObjectNode existingItem = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setCurrentVariation("projecta");
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid("test pid");
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "test1A";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    expectedMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    expectedMap.put("projectageneraltestdoc.generalTestDocValue", "test1A");
    expectedMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    expectedMap.put("projectbgeneraltestdoc.generalTestDocValue", "projectbTestDoc");

    JsonNode expected = mapper.valueToTree(expectedMap);

    JsonNode actual = inducer.induce(ProjectAGeneralTestDoc.class, item, existingItem);

    assertEquals(expected, actual);
  }

  /* 
   * Test when an entity with two variations on a general property
   * is updated on the key that is not the general accepted.
   */
  @Test
  public void testInduceDomainEntitySecondVariationUpdated() throws VariationException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "testB");
    existingMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    existingMap.put("projectbgeneraltestdoc.generalTestDocValue", "projectbTestDoc");

    ObjectNode existingItem = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setCurrentVariation("projecta");
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid("test pid");
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "test1A";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "testB");
    expectedMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    expectedMap.put("projectageneraltestdoc.generalTestDocValue", "test1A");
    expectedMap.put("projectbgeneraltestdoc.generalTestDocValue", "projectbTestDoc");

    JsonNode expected = mapper.valueToTree(expectedMap);

    JsonNode actual = inducer.induce(ProjectAGeneralTestDoc.class, item, existingItem);

    assertEquals(expected, actual);
  }

  @Test
  public void testInduceDomainEntityWithRole() throws VariationException {
    ProjectBGeneralTestDoc item = createProjectBGeneralTestDoc();
    item.generalTestDocValue = "test";
    ProjectBTestRole role = new ProjectBTestRole();
    role.setBeeName("beeName");
    role.setRoleName("roleName");
    List<Role> roles = Lists.newArrayList();
    roles.add(role);
    item.setRoles(roles);

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    expectedMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    expectedMap.put("projectbtestrole.beeName", "beeName");
    expectedMap.put("testrole.roleName", "roleName");

    JsonNode expected = mapper.valueToTree(expectedMap);

    JsonNode actual = inducer.induce(ProjectBGeneralTestDoc.class, item);

    assertEquals(expected, actual);

  }

  @Test
  public void testInduceUpdatedDomainEntityWithRoleNewVariation() throws VariationException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    existingMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    existingMap.put("projectbtestrole.beeName", "beeName");
    existingMap.put("testrole.roleName", "roleName");

    ObjectNode existing = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setCurrentVariation("projecta");
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID), new Reference(ProjectBGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid("test pid");
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "testA";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    expectedMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    expectedMap.put("projectageneraltestdoc.generalTestDocValue", "testA");
    expectedMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    expectedMap.put("projectbtestrole.beeName", "beeName");
    expectedMap.put("testrole.roleName", "roleName");
    ObjectNode expected = mapper.valueToTree(expectedMap);

    JsonNode actual = inducer.induce(ProjectAGeneralTestDoc.class, item, existing);

    assertEquals(expected, actual);

  }

  @Test
  public void testInduceUpdatedDomainEntityWithRolesNewRole() throws VariationException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    existingMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    existingMap.put("projectbtestrole.beeName", "beeName");
    existingMap.put("testrole.roleName", "roleName");

    ObjectNode existing = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setCurrentVariation("projecta");
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID), new Reference(ProjectBGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid("test pid");
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "testA";

    List<Role> roles = Lists.newArrayList();
    ProjectANewTestRole role = new ProjectANewTestRole();
    role.setNewTestRoleName("newTestRoleName");
    role.setProjectANewTestRoleName("projectANewTestRoleName");
    roles.add(role);

    item.setRoles(roles);

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    expectedMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    expectedMap.put("projectageneraltestdoc.generalTestDocValue", "testA");
    expectedMap.put("projectanewtestrole.projectANewTestRoleName", "projectANewTestRoleName");
    expectedMap.put("newtestrole.newTestRoleName", "newTestRoleName");
    expectedMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    expectedMap.put("projectbtestrole.beeName", "beeName");
    expectedMap.put("testrole.roleName", "roleName");
    ObjectNode expected = mapper.valueToTree(expectedMap);

    JsonNode actual = inducer.induce(ProjectAGeneralTestDoc.class, item, existing);

    assertEquals(expected, actual);
  }

  @Test
  public void testInduceUpdatedDomainEntityWithRolesNewVariationForRole() throws VariationException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    existingMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    existingMap.put("projectbtestrole.beeName", "beeName");
    existingMap.put("testrole.roleName", "roleName");

    ObjectNode existing = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setCurrentVariation("projecta");
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID), new Reference(ProjectBGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid("test pid");
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "testA";

    List<Role> roles = Lists.newArrayList();
    ProjectATestRole role = new ProjectATestRole();
    role.setProjectATestRoleName("projectATestRoleName");
    role.setRoleName("projectARoleName");
    roles.add(role);

    item.setRoles(roles);

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test pid", "test");
    expectedMap.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    expectedMap.put("projectageneraltestdoc.generalTestDocValue", "testA");
    expectedMap.put("projectatestrole.projectATestRoleName", "projectATestRoleName");
    expectedMap.put("projectatestrole.roleName", "projectARoleName");
    expectedMap.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    expectedMap.put("projectbtestrole.beeName", "beeName");
    expectedMap.put("testrole.roleName", "roleName");
    ObjectNode expected = mapper.valueToTree(expectedMap);

    JsonNode actual = inducer.induce(ProjectAGeneralTestDoc.class, item, existing);

    assertEquals(expected, actual);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInduceNullEntity() throws VariationException {
    inducer.induce(ProjectBGeneralTestDoc.class, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInduceNullType() throws VariationException {
    inducer.induce(null, new ProjectBGeneralTestDoc());
  }
}
