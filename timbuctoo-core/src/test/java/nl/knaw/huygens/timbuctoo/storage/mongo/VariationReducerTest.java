package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.model.TestSystemEntityPrimitive;
import nl.knaw.huygens.timbuctoo.model.TestSystemEntityPrimitiveCollections;
import nl.knaw.huygens.timbuctoo.variation.model.GeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.NewTestRole;
import nl.knaw.huygens.timbuctoo.variation.model.TestRole;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectANewTestRole;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectATestRole;
import nl.knaw.huygens.timbuctoo.variation.model.projectb.ProjectBGeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projectb.ProjectBTestRole;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class VariationReducerTest extends VariationTestBase {

  private static final String GENERAL_TEST_DOC_VALUE = "generalTestDocValue";
  private static final String TEST_PID = "test pid";
  private static TypeRegistry registry;
  private static MongoObjectMapper mongoObjectMapper;
  private static MongoFieldMapper mongoFieldMapper;

  private static final String TEST_ID = "id0000000001";
  private VariationReducer reducer;
  private ObjectMapper mapper;

  @BeforeClass
  public static void setupRegistry() {
    registry = new TypeRegistry("timbuctoo.variation.model timbuctoo.variation.model.projecta timbuctoo.variation.model.projectb");
    mongoFieldMapper = new MongoFieldMapper();
    mongoObjectMapper = new MongoObjectMapper(mongoFieldMapper);
  }

  @Before
  public void setUp() {
    reducer = new VariationReducer(registry, mongoObjectMapper, mongoFieldMapper);
    mapper = new ObjectMapper();
  }

  @After
  public void tearDown() {
    reducer = null;
    mapper = null;
  }

  @Test
  public void testReduceSystemEntity() throws JsonProcessingException, IOException {
    String name = "test";
    String testValue1 = "testValue1";
    String testValue2 = "testValue2";
    ObjectNode item = createSystemObjectNode(TEST_ID, name, testValue1, testValue2);

    TestSystemEntity expected = new TestSystemEntity();
    expected.setId(TEST_ID);
    expected.setName(name);
    expected.setTestValue1(testValue1);
    expected.setTestValue2(testValue2);

    TestSystemEntity actual = reducer.reduce(TestSystemEntity.class, item);

    assertEquals(expected, actual);

  }

  @Test
  public void testReducePrimitiveFields() throws VariationException, JsonProcessingException {
    Map<String, Object> map = Maps.newHashMap();
    map.put("testsystementityprimitive.testBoolean", "true");
    map.put("testsystementityprimitive.testChar", "r");
    map.put("testsystementityprimitive.testDouble", "3.14");
    map.put("testsystementityprimitive.testFloat", "2.13");
    map.put("testsystementityprimitive.testInt", "14");
    map.put("testsystementityprimitive.testLong", "15098");
    map.put("testsystementityprimitive.testShort", "4");

    ObjectNode node = mapper.valueToTree(map);

    TestSystemEntityPrimitive expected = new TestSystemEntityPrimitive();
    expected.setTestBoolean(true);
    expected.setTestChar('r');
    expected.setTestDouble(3.14);
    expected.setTestFloat(2.13f);
    expected.setTestInt(14);
    expected.setTestLong(15098l);
    expected.setTestShort((short) 4);

    TestSystemEntityPrimitive actual = reducer.reduce(TestSystemEntityPrimitive.class, node);

    assertEquals(expected, actual);
  }

  @Test
  public void testReduceSystemEntityPrimitiveCollections() throws VariationException, JsonProcessingException {
    Map<String, Object> map = Maps.newHashMap();
    map.put("testsystementityprimitivecollections.testStringList", new String[] { "test", "test1" });
    map.put("testsystementityprimitivecollections.testIntegerList", new Integer[] { 1, 13, 42 });

    ObjectNode node = mapper.valueToTree(map);

    TestSystemEntityPrimitiveCollections expected = new TestSystemEntityPrimitiveCollections();
    expected.setTestIntegerList(Lists.newArrayList(1, 13, 42));
    expected.setTestStringList(Lists.newArrayList("test", "test1"));

    TestSystemEntityPrimitiveCollections actual = reducer.reduce(TestSystemEntityPrimitiveCollections.class, node);

    assertEquals(expected, actual);
  }

  @Test
  public void testReduceDomainEntityDefault() throws VariationException, JsonProcessingException {
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, TEST_PID, GENERAL_TEST_DOC_VALUE);
    map.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    ObjectNode node = mapper.valueToTree(map);

    GeneralTestDoc expected = new GeneralTestDoc();
    expected.setId(TEST_ID);
    expected.setPid(TEST_PID);
    expected.generalTestDocValue = GENERAL_TEST_DOC_VALUE;

    GeneralTestDoc actual = reducer.reduce(GeneralTestDoc.class, node);

    assertEquals(expected, actual);
  }

  @Test
  public void testReduceDomainEntityProjectSubClass() throws VariationException, JsonProcessingException {
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, TEST_PID, GENERAL_TEST_DOC_VALUE);
    String projectatestvalue = "projectatest";
    map.put("projectageneraltestdoc.projectAGeneralTestDocValue", projectatestvalue);

    ObjectNode node = mapper.valueToTree(map);

    ProjectAGeneralTestDoc expected = new ProjectAGeneralTestDoc();
    expected.setId(TEST_ID);
    expected.setPid(TEST_PID);
    expected.generalTestDocValue = GENERAL_TEST_DOC_VALUE;
    expected.projectAGeneralTestDocValue = projectatestvalue;

    ProjectAGeneralTestDoc actual = reducer.reduce(ProjectAGeneralTestDoc.class, node);

    assertEquals(expected, actual);
  }

  @Test
  public void testReduceDomainEntityProjectSubClassVariation() throws VariationException, JsonProcessingException {
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, TEST_PID, GENERAL_TEST_DOC_VALUE);
    String projectatestvalue = "projectatest";
    map.put("projectageneraltestdoc.projectAGeneralTestDocValue", projectatestvalue);
    String projectAVariation = "projectAVariation";
    map.put("projectageneraltestdoc.generalTestDocValue", projectAVariation);

    ObjectNode node = mapper.valueToTree(map);

    ProjectAGeneralTestDoc expected = new ProjectAGeneralTestDoc();
    expected.setId(TEST_ID);
    expected.setPid(TEST_PID);
    expected.generalTestDocValue = projectAVariation;
    expected.projectAGeneralTestDocValue = projectatestvalue;

    ProjectAGeneralTestDoc actual = reducer.reduce(ProjectAGeneralTestDoc.class, node);

    assertEquals(expected, actual);
  }

  @Test
  public void testReduceDomainEntityWithRole() throws VariationException, JsonProcessingException {
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, TEST_PID, GENERAL_TEST_DOC_VALUE);
    map.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    map.put("projectbtestrole.beeName", "beeName");
    map.put("testrole.roleName", "roleName");

    JsonNode node = mapper.valueToTree(map);

    GeneralTestDoc expected = new GeneralTestDoc();
    expected.setId(TEST_ID);
    expected.setPid(TEST_PID);
    expected.generalTestDocValue = GENERAL_TEST_DOC_VALUE;
    TestRole testRole = new TestRole();
    testRole.setRoleName("roleName");
    ArrayList<Role> roles = Lists.newArrayList();
    roles.add(testRole);
    expected.setRoles(roles);

    GeneralTestDoc actual = reducer.reduce(GeneralTestDoc.class, node);

    assertEquals(expected, actual);
  }

  @Test
  public void testReduceDomainEntityWithMultipleRoles() throws VariationException, JsonProcessingException {
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, TEST_PID, GENERAL_TEST_DOC_VALUE);
    map.put("projectageneraltestdoc.projectAGeneralTestDocValue", "testB");
    map.put("projectatestrole.projectANewTestRoleName", "beeName");
    map.put("testrole.roleName", "roleName");
    map.put("newtestrole.newTestRoleName", "newTestRoleName");
    map.put("projectanewtestrole.projectANewTestRoleName", "projectANewTestRoleName");

    JsonNode node = mapper.valueToTree(map);

    GeneralTestDoc expected = new GeneralTestDoc();
    expected.setId(TEST_ID);
    expected.setPid(TEST_PID);
    expected.generalTestDocValue = GENERAL_TEST_DOC_VALUE;
    TestRole testRole = new TestRole();
    testRole.setRoleName("roleName");
    NewTestRole newTestRole = new NewTestRole();
    newTestRole.setNewTestRoleName("newTestRoleName");
    ArrayList<Role> roles = Lists.newArrayList();
    roles.add(testRole);
    roles.add(newTestRole);
    expected.setRoles(roles);

    GeneralTestDoc actual = reducer.reduce(GeneralTestDoc.class, node);

    assertEquals(expected, actual);
  }

  @Test
  public void testReduceDomainEntityProjectSubClassWithRole() throws VariationException, JsonProcessingException {
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, TEST_PID, GENERAL_TEST_DOC_VALUE);
    String projectBTestDocValue = "testB";
    map.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", projectBTestDocValue);
    map.put("projectbtestrole.beeName", "beeName");
    map.put("testrole.roleName", "roleName");

    JsonNode node = mapper.valueToTree(map);

    ProjectBGeneralTestDoc expected = new ProjectBGeneralTestDoc();
    expected.setId(TEST_ID);
    expected.setPid(TEST_PID);
    expected.generalTestDocValue = GENERAL_TEST_DOC_VALUE;
    expected.projectBGeneralTestDocValue = projectBTestDocValue;
    ProjectBTestRole testRole = new ProjectBTestRole();
    testRole.setRoleName("roleName");
    testRole.setBeeName("beeName");

    ArrayList<Role> roles = Lists.newArrayList();
    roles.add(testRole);
    expected.setRoles(roles);

    ProjectBGeneralTestDoc actual = reducer.reduce(ProjectBGeneralTestDoc.class, node);

    assertEquals(expected, actual);
  }

  @Test
  public void testReduceDomainEnityProjectSubClassWithRoleNotFilledIn() throws VariationException, JsonProcessingException {
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, TEST_PID, GENERAL_TEST_DOC_VALUE);
    map.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    map.put("projectbtestrole.beeName", "beeName");
    map.put("testrole.roleName", "roleName");

    JsonNode node = mapper.valueToTree(map);

    ProjectAGeneralTestDoc expected = new ProjectAGeneralTestDoc();
    expected.setId(TEST_ID);
    expected.setPid(TEST_PID);
    expected.generalTestDocValue = GENERAL_TEST_DOC_VALUE;
    ProjectATestRole testRole = new ProjectATestRole();
    testRole.setRoleName("roleName");

    ArrayList<Role> roles = Lists.newArrayList();
    roles.add(testRole);
    expected.setRoles(roles);

    ProjectAGeneralTestDoc actual = reducer.reduce(ProjectAGeneralTestDoc.class, node);

    assertEquals(expected, actual);
  }

  @Test
  public void testReduceDomainEnityProjectSubClassWithUndefinedSubRole() {

  }

  @Test
  public void testReduceDomainEntityProjectSubClassWithMultipleRoles() throws VariationException, JsonProcessingException {
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, TEST_PID, "test");
    map.put("projectageneraltestdoc.projectAGeneralTestDocValue", "testB");
    map.put("projectatestrole.projectATestRoleName", "beeName");
    map.put("testrole.roleName", "roleName");
    map.put("newtestrole.newTestRoleName", "newTestRoleName");
    map.put("projectanewtestrole.projectANewTestRoleName", "projectANewTestRoleName");

    JsonNode node = mapper.valueToTree(map);

    ProjectAGeneralTestDoc expected = new ProjectAGeneralTestDoc();
    expected.setId(TEST_ID);
    expected.setPid(TEST_PID);
    expected.generalTestDocValue = "test";
    expected.projectAGeneralTestDocValue = "testB";
    ProjectATestRole role = new ProjectATestRole();
    role.setProjectATestRoleName("beeName");
    role.setRoleName("roleName");
    ProjectANewTestRole projectANewTestRole = new ProjectANewTestRole();
    projectANewTestRole.setNewTestRoleName("newTestRoleName");
    projectANewTestRole.setProjectANewTestRoleName("projectANewTestRoleName");
    List<Role> roles = Lists.newArrayList();
    roles.add(role);
    roles.add(projectANewTestRole);
    expected.setRoles(roles);

    ProjectAGeneralTestDoc actual = reducer.reduce(ProjectAGeneralTestDoc.class, node);

    assertEquals(expected, actual);

  }

  @Test
  public void testReduceDomainEntityRequestedVariation() throws VariationException, JsonProcessingException {
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, TEST_PID, GENERAL_TEST_DOC_VALUE);
    String projectatestvalue = "projectatest";
    map.put("projectageneraltestdoc.projectAGeneralTestDocValue", projectatestvalue);
    String projectAVariation = "projectAVariation";
    map.put("projectageneraltestdoc.generalTestDocValue", projectAVariation);

    ObjectNode node = mapper.valueToTree(map);

    GeneralTestDoc expected = new GeneralTestDoc();
    expected.setId(TEST_ID);
    expected.setPid(TEST_PID);
    expected.generalTestDocValue = projectAVariation;

    GeneralTestDoc actual = reducer.reduce(GeneralTestDoc.class, node, "projecta");

    assertEquals(expected, actual);
  }

  @Test
  public void testReduceDomainEntityWithRoleRequestedVariation() throws VariationException, JsonProcessingException {
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, TEST_PID, "test");
    map.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    map.put("projectbtestrole.beeName", "beeName");
    map.put("testrole.roleName", "roleName");
    String projectatestvalue = "projectatest";
    map.put("projectageneraltestdoc.projectAGeneralTestDocValue", projectatestvalue);
    String projectAVariation = "projectAVariation";
    map.put("projectageneraltestdoc.generalTestDocValue", projectAVariation);
    map.put("projectatestrole.projectATestRoleName", "value");
    map.put("projectatestrole.roleName", "value");

    JsonNode node = mapper.valueToTree(map);

    GeneralTestDoc expected = new GeneralTestDoc();
    expected.setId(TEST_ID);
    expected.setPid(TEST_PID);
    expected.generalTestDocValue = projectAVariation;
    TestRole testRole = new TestRole();
    testRole.setRoleName("value");
    ArrayList<Role> roles = Lists.newArrayList();
    roles.add(testRole);
    expected.setRoles(roles);

    GeneralTestDoc actual = reducer.reduce(GeneralTestDoc.class, node, "projecta");

    assertEquals(expected, actual);
  }

  @Test
  public void testGetAllForDBObject() throws IOException {
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, TEST_PID, "test");
    map.put("projectbgeneraltestdoc.projectBGeneralTestDocValue", "testB");
    map.put("projectbtestrole.beeName", "beeName");
    map.put("testrole.roleName", "roleName");
    String projectatestvalue = "projectatest";
    map.put("projectageneraltestdoc.projectAGeneralTestDocValue", projectatestvalue);
    String projectAVariation = "projectAVariation";
    map.put("projectageneraltestdoc.generalTestDocValue", projectAVariation);
    map.put("projectatestrole.projectATestRoleName", "value");
    map.put("projectatestrole.roleName", "value");

    JsonNode node = mapper.valueToTree(map);

    List<? extends Entity> variation = reducer.getAllForDBObject(new DBJsonNode(node), ProjectAGeneralTestDoc.class);

    assertEquals(5, variation.size());
  }

  @Override
  protected ObjectMapper getMapper() {
    return this.mapper;
  }

  //TODO: add tests for reducing revisions / multiple revisions and getAllForObject.

}
