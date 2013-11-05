package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.GeneralTestDoc;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class VariationReducerTest extends VariationTestBase {

  private static TypeRegistry registry;

  private static final String TEST_ID = "id0000000001";
  private VariationReducer reducer;
  private ObjectMapper mapper;

  @BeforeClass
  public static void setupRegistry() {
    registry = new TypeRegistry("timbuctoo.variation.model timbuctoo.variation.model.projecta timbuctoo.variation.model.projectb");
  }

  @Before
  public void setUp() {
    reducer = new VariationReducer(registry);
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
  public void testReduceDomainEntityDefault() throws VariationException, JsonProcessingException {
    String pid = "test pid";
    String generalTestDocValue = "generalTestDocValue";
    Map<String, Object> map = createGeneralTestDocMap(TEST_ID, pid, generalTestDocValue);
    map.put("projectageneraltestdoc.projectAGeneralTestDocValue", "projectatest");
    ObjectNode node = mapper.valueToTree(map);

    GeneralTestDoc expected = new GeneralTestDoc();
    expected.setId(TEST_ID);
    expected.setPid(pid);
    expected.generalTestDocValue = generalTestDocValue;

    GeneralTestDoc actual = reducer.reduce(GeneralTestDoc.class, node);

    assertEquals(expected, actual);
  }

  @Test
  public void testReduceDomainEntityProjectSubClass() {
    fail("Yet to be implemented");
  }

  @Test
  public void testReduceDomainEntityProjectSubClassVariation() {
    fail("Yet to be implemented");
  }

  @Test
  public void testReduceDomainEntityWithRoles() {
    fail("Yet to be implemented");
  }

  @Test
  public void testReduceDomainEntityProjectSubClassWithRoles() {
    fail("Yet to be implemented");
  }

  @Override
  protected ObjectMapper getMapper() {
    return this.mapper;
  }

  //TODO: add tests for reducing revisions / multiple revisions and getAllForObject.

}
