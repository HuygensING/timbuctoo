package nl.knaw.huygens.timbuctoo.storage.mongo;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DatableSystemEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.variation.model.GeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestConcreteDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectANewTestRole;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectATestDocWithPersonName;
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

public class VariationInducerTest extends VariationTestBase {

  private static final String DEFAULT_PID = "test pid";
  private static final String DEFAULT_DOMAIN_ID = "GTD0000000012";
  private static final String TEST_NAME = "test";
  private final static String TEST_SYSTEM_ID = "TSD";
  private static TypeRegistry registry;

  ObjectMapper mapper;
  private VariationInducer inducer;

  @BeforeClass
  public static void setupMapper() {
    registry = new TypeRegistry("timbuctoo.variation.model timbuctoo.variation.model.projecta timbuctoo.variation.model.projectb timbuctoo.model");
  }

  @Before
  public void setUp() throws Exception {
    mapper = new ObjectMapper();
    inducer = new VariationInducer(registry);
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

    assertEquals(expected, inducer.induce(TestSystemEntity.class, doc));
  }

  @Test
  public void testInduceSystemEntityWithNullValues() throws JsonProcessingException, IOException {
    String testValue1 = "value";

    JsonNode expected = createSystemObjectNode(TEST_SYSTEM_ID, null, testValue1, null);

    TestSystemEntity doc = new TestSystemEntity();
    doc.setId(TEST_SYSTEM_ID);
    doc.setTestValue1(testValue1);

    assertEquals(expected, inducer.induce(TestSystemEntity.class, doc));
  }

  @Test
  public void testInduceUpdatedSystemEntity() throws JsonProcessingException, IOException {
    String testValue1 = "value";
    String testValue2 = "testValue2";
    String name2 = "name2";

    JsonNode expectedObject = createSystemObjectNode(TEST_SYSTEM_ID, name2, testValue1, testValue2);
    ObjectNode existingObject = createSystemObjectNode(TEST_SYSTEM_ID, TEST_NAME, testValue1, null);

    TestSystemEntity item = new TestSystemEntity();
    item.setId(TEST_SYSTEM_ID);
    item.setName(name2);
    item.setTestValue1(testValue1);
    item.setTestValue2(testValue2);

    assertEquals(expectedObject, inducer.induce(TestSystemEntity.class, item, existingObject));
  }

  @Test
  public void testInduceDatable() throws StorageException {
    Map<String, Object> expectedMap = Maps.newHashMap();
    Datable datable = new Datable("20131011");
    expectedMap.put(propertyName("datablesystementity", "testDatable"), datable.getEDTF());
    expectedMap.put("^rev", 0);
    JsonNode expected = mapper.valueToTree(expectedMap);

    DatableSystemEntity item = new DatableSystemEntity();
    item.setTestDatable(datable);

    assertEquals(expected, inducer.induce(DatableSystemEntity.class, item));
  }

  @Test
  public void testInduceDomainEntityPrimitive() throws StorageException {
    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, "test_pid", "testDocValue");
    expectedMap.put(propertyName("testconcretedoc", "name"), "test");
    JsonNode expected = mapper.valueToTree(expectedMap);

    GeneralTestDoc item = new GeneralTestDoc();
    item.name = "test";
    item.generalTestDocValue = "testDocValue";
    item.setPid("test_pid");
    item.setId(DEFAULT_DOMAIN_ID);

    assertEquals(expected, inducer.induce(GeneralTestDoc.class, item));
  }

  @Ignore("#1909")
  @Test
  public void testInduceUpdatedDomainEntityPrimitive() throws StorageException {
    Map<String, Object> existingMap = createTestConcreteDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    ObjectNode node = mapper.valueToTree(existingMap);

    Map<String, Object> expectedMap = createTestConcreteDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test1");
    JsonNode expected = mapper.valueToTree(expectedMap);

    TestConcreteDoc item = new TestConcreteDoc();
    item.name = "test1";
    item.setPid(DEFAULT_PID);
    item.setId(DEFAULT_DOMAIN_ID);

    assertEquals(expected, inducer.induce(TestConcreteDoc.class, item, node));
  }

  @Test
  public void testInduceDomainEntityProject() throws StorageException {
    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid(DEFAULT_PID);
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "test";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    expectedMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    JsonNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectAGeneralTestDoc.class, item));
  }

  @Test
  public void testInduceDomainEntityProjectWithPersonName() throws StorageException {
    ProjectATestDocWithPersonName item = new ProjectATestDocWithPersonName();
    PersonName name = new PersonName();
    name.addNameComponent(Type.FORENAME, "test");
    name.addNameComponent(Type.SURNAME, "test");
    item.setPersonName(name);

    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put(propertyName("projectatestdocwithpersonname", "personName"), PersonNameMapper.createPersonNameMap(name));
    expectedMap.put("^rev", 0);
    expectedMap.put(DomainEntity.DELETED, false);
    JsonNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectATestDocWithPersonName.class, item));
  }

  @Test
  public void testInduceDomainEntityNewVariationAddValue() throws StorageException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    existingMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    ObjectNode existing = mapper.valueToTree(existingMap);

    ProjectBGeneralTestDoc item = createProjectBGeneralTestDoc(DEFAULT_DOMAIN_ID, DEFAULT_PID, "testB");

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    expectedMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "projectBGeneralTestDocValue"), "testB");
    JsonNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectBGeneralTestDoc.class, item, existing));
  }

  @Test
  public void testInduceDomainEntityNewVariationExistingValue() throws StorageException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    existingMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    ObjectNode existing = mapper.valueToTree(existingMap);

    ProjectBGeneralTestDoc item = createProjectBGeneralTestDoc(DEFAULT_DOMAIN_ID, DEFAULT_PID, "testB");
    item.generalTestDocValue = "projectbTestDoc";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    expectedMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "projectBGeneralTestDocValue"), "testB");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "generalTestDocValue"), "projectbTestDoc");
    JsonNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectBGeneralTestDoc.class, item, existing));
  }

  /*
   * Project value equals to the default value is updated.
   */
  @Test
  public void testInduceDomainEntityVariationUpdated() throws StorageException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    existingMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    existingMap.put(propertyName("projectbgeneraltestdoc", "projectBGeneralTestDocValue"), "testB");
    existingMap.put(propertyName("projectbgeneraltestdoc", "generalTestDocValue"), "projectbTestDoc");

    ObjectNode existingItem = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid(DEFAULT_PID);
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "test1A";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    expectedMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    expectedMap.put(propertyName("projectageneraltestdoc", "generalTestDocValue"), "test1A");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "projectBGeneralTestDocValue"), "testB");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "generalTestDocValue"), "projectbTestDoc");
    JsonNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectAGeneralTestDoc.class, item, existingItem));
  }

  /* 
   * Test when an entity with two variations on a general property
   * is updated on the key that is not the general accepted.
   */
  @Test
  public void testInduceDomainEntitySecondVariationUpdated() throws StorageException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "testB");
    existingMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    existingMap.put(propertyName("projectbgeneraltestdoc", "generalTestDocValue"), "projectbTestDoc");

    ObjectNode existingItem = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid(DEFAULT_PID);
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "test1A";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "testB");
    expectedMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    expectedMap.put(propertyName("projectageneraltestdoc", "generalTestDocValue"), "test1A");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "generalTestDocValue"), "projectbTestDoc");
    JsonNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectAGeneralTestDoc.class, item, existingItem));
  }

  @Test
  public void testInduceProjectVariationUpdatedWithExistingValue() throws StorageException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "testB");
    existingMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    existingMap.put(propertyName("projectbgeneraltestdoc", "generalTestDocValue"), "projectbTestDoc");

    ObjectNode existingItem = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid(DEFAULT_PID);
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "testB";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "testB");
    expectedMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "generalTestDocValue"), "projectbTestDoc");
    JsonNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectAGeneralTestDoc.class, item, existingItem));
  }

  @Test
  public void testInduceProjectVariationUpdatedToDefault() throws StorageException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "testB");
    existingMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    existingMap.put(propertyName("projectageneraltestdoc", "generalTestDocValue"), "testB");
    existingMap.put(propertyName("projectbgeneraltestdoc", "generalTestDocValue"), "projectbTestDoc");

    ObjectNode existingItem = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid(DEFAULT_PID);
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "testB";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "testB");
    expectedMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "generalTestDocValue"), "projectbTestDoc");
    JsonNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectAGeneralTestDoc.class, item, existingItem));
  }

  @Test
  public void testInduceDomainEntityWithRole() throws StorageException {
    ProjectBGeneralTestDoc item = createProjectBGeneralTestDoc(DEFAULT_DOMAIN_ID, DEFAULT_PID, "testB");
    item.generalTestDocValue = "test";
    ProjectBTestRole role = new ProjectBTestRole();
    role.setBeeName("beeName");
    role.setRoleName("roleName");
    List<Role> roles = Lists.newArrayList();
    roles.add(role);
    item.setRoles(roles);

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "projectBGeneralTestDocValue"), "testB");
    expectedMap.put(propertyName("projectbtestrole", "beeName"), "beeName");
    expectedMap.put(propertyName("testrole", "roleName"), "roleName");
    JsonNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectBGeneralTestDoc.class, item));
  }

  @Test
  public void testInduceUpdatedDomainEntityWithRoleNewVariation() throws StorageException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    existingMap.put(propertyName("projectbgeneraltestdoc", "projectBGeneralTestDocValue"), "testB");
    existingMap.put(propertyName("projectbtestrole", "beeName"), "beeName");
    existingMap.put(propertyName("testrole", "roleName"), "roleName");

    ObjectNode existing = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID), new Reference(ProjectBGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid(DEFAULT_PID);
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "testA";

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    expectedMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    expectedMap.put(propertyName("projectageneraltestdoc", "generalTestDocValue"), "testA");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "projectBGeneralTestDocValue"), "testB");
    expectedMap.put(propertyName("projectbtestrole", "beeName"), "beeName");
    expectedMap.put(propertyName("testrole", "roleName"), "roleName");
    ObjectNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectAGeneralTestDoc.class, item, existing));
  }

  @Test
  public void testInduceUpdatedDomainEntityWithRolesNewRole() throws StorageException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    existingMap.put(propertyName("projectbgeneraltestdoc", "projectBGeneralTestDocValue"), "testB");
    existingMap.put(propertyName("projectbtestrole", "beeName"), "beeName");
    existingMap.put(propertyName("testrole", "roleName"), "roleName");

    ObjectNode existing = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID), new Reference(ProjectBGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid(DEFAULT_PID);
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "testA";

    List<Role> roles = Lists.newArrayList();
    ProjectANewTestRole role = new ProjectANewTestRole();
    role.setNewTestRoleName("newTestRoleName");
    role.setProjectANewTestRoleName("projectANewTestRoleName");
    roles.add(role);

    item.setRoles(roles);

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    expectedMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    expectedMap.put(propertyName("projectageneraltestdoc", "generalTestDocValue"), "testA");
    expectedMap.put(propertyName("projectanewtestrole", "projectANewTestRoleName"), "projectANewTestRoleName");
    expectedMap.put(propertyName("newtestrole", "newTestRoleName"), "newTestRoleName");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "projectBGeneralTestDocValue"), "testB");
    expectedMap.put(propertyName("projectbtestrole", "beeName"), "beeName");
    expectedMap.put(propertyName("testrole", "roleName"), "roleName");
    ObjectNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectAGeneralTestDoc.class, item, existing));
  }

  @Test
  public void testInduceUpdatedDomainEntityWithRolesNewVariationForRole() throws StorageException {
    Map<String, Object> existingMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    existingMap.put(propertyName("projectbgeneraltestdoc", "projectBGeneralTestDocValue"), "testB");
    existingMap.put(propertyName("projectbtestrole", "beeName"), "beeName");
    existingMap.put(propertyName("testrole", "roleName"), "roleName");

    ObjectNode existing = mapper.valueToTree(existingMap);

    ProjectAGeneralTestDoc item = new ProjectAGeneralTestDoc();
    item.setId(DEFAULT_DOMAIN_ID);
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, DEFAULT_DOMAIN_ID), new Reference(ProjectBGeneralTestDoc.class, DEFAULT_DOMAIN_ID)));
    item.setPid(DEFAULT_PID);
    item.projectAGeneralTestDocValue = "projectatest";
    item.generalTestDocValue = "testA";

    List<Role> roles = Lists.newArrayList();
    ProjectATestRole role = new ProjectATestRole();
    role.setProjectATestRoleName("projectATestRoleName");
    role.setRoleName("projectARoleName");
    roles.add(role);

    item.setRoles(roles);

    Map<String, Object> expectedMap = createGeneralTestDocMap(DEFAULT_DOMAIN_ID, DEFAULT_PID, "test");
    expectedMap.put(propertyName("projectageneraltestdoc", "projectAGeneralTestDocValue"), "projectatest");
    expectedMap.put(propertyName("projectageneraltestdoc", "generalTestDocValue"), "testA");
    expectedMap.put(propertyName("projectatestrole", "projectATestRoleName"), "projectATestRoleName");
    expectedMap.put(propertyName("projectatestrole", "roleName"), "projectARoleName");
    expectedMap.put(propertyName("projectbgeneraltestdoc", "projectBGeneralTestDocValue"), "testB");
    expectedMap.put(propertyName("projectbtestrole", "beeName"), "beeName");
    expectedMap.put(propertyName("testrole", "roleName"), "roleName");
    ObjectNode expected = mapper.valueToTree(expectedMap);

    assertEquals(expected, inducer.induce(ProjectAGeneralTestDoc.class, item, existing));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInduceNullEntity() throws StorageException {
    inducer.induce(ProjectBGeneralTestDoc.class, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInduceNullType() throws StorageException {
    inducer.induce(null, new ProjectBGeneralTestDoc());
  }

  @Override
  protected ObjectMapper getMapper() {
    return this.mapper;
  }

}
