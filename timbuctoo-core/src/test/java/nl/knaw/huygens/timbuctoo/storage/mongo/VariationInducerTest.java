package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projectb.TestDoc;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class VariationInducerTest {

  private static TypeRegistry registry;

  private ObjectMapper mapper;
  private VariationInducer inducer;

  @BeforeClass
  public static void setupRegistry() {
    registry = new TypeRegistry("timbuctoo.variation.model timbuctoo.variation.model.projecta timbuctoo.variation.model.projectb");
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
  public void testInduce() throws IOException {
    String testStr = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\"]}],\"!defaultVRE\":\"projectb\"}," + "\"projectb-testdoc\": {\"blah\": \"stuff\"},\"_id\":null,"
        + "\"^rev\":0,\"^lastChange\":null,\"^creation\":null, \"^pid\":null, \"^deleted\":false}";

    JsonNode t = mapper.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceWithSubModels() throws IOException {
    String testStr = "{\"projecta-projectageneraltestdoc\": {\"projectAGeneralTestDocValue\": \"other stuff\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"stuff\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"testconcretedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"_id\":null,\"^pid\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    JsonNode t = mapper.readTree(testStr);
    ProjectAGeneralTestDoc x = new ProjectAGeneralTestDoc();
    x.name = "x";
    x.generalTestDocValue = "stuff";
    x.projectAGeneralTestDocValue = "other stuff";

    JsonNode allVariations = inducer.induce(x, ProjectAGeneralTestDoc.class);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceIgnoreVariationProperty() throws IOException {
    String testStr = "{\"projecta-projectageneraltestdoc\": {\"projectAGeneralTestDocValue\": \"other stuff\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"stuff\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"testconcretedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"_id\":null,\"^pid\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    JsonNode t = mapper.readTree(testStr);
    ProjectAGeneralTestDoc x = new ProjectAGeneralTestDoc();
    x.name = "x";
    x.generalTestDocValue = "stuff";
    x.projectAGeneralTestDocValue = "other stuff";
    x.setVariations(null);

    JsonNode allVariations = inducer.induce(x, ProjectAGeneralTestDoc.class);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceExisting() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"other\"]}],\"!defaultVRE\":\"other\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
        + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    String testStr = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"other\"]}, {\"v\":\"x\", \"a\":[\"projectb\"]}],\"!defaultVRE\":\"other\"}, "
        + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
        + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    JsonNode t = mapper.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceAgreeWithExisting() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\"]}],\"!defaultVRE\":\"other\"}," + "\"other\": {\"blub\": \"otherstuff\"},"
        + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";
    String testStr = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\",\"projectb\"]}],\"!defaultVRE\":\"other\"}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
        + "\"other\": {\"blub\": \"otherstuff\"}," + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    JsonNode t = mapper.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceExistingWithNewSubModel() throws IOException {
    String inTree = "{\"projectb-projectbotherconcretedoc\":{\"someOtherProp\":\"blah2\"},"
        + "\"otherconcretedoc\":{\"someProp\":[{\"v\":\"blah\", \"a\":[\"projectb\"]}],\"!defaultVRE\":\"projectb\"},"
        + "\"testconcretedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\",\"projecta\"]}],\"!defaultVRE\":\"projectb\"},"
        + "\"_id\":null,\"^pid\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"projecta-projectageneraltestdoc\": {\"projectAGeneralTestDocValue\": \"other stuff\"},"
        + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"stuff\", \"a\":[\"projecta\"]}],\"!defaultVRE\":\"projecta\"},"
        + "\"projectb-projectbotherconcretedoc\":{\"someOtherProp\":\"blah2\"}," + "\"otherconcretedoc\":{\"someProp\":[{\"v\":\"blah\", \"a\":[\"projectb\"]}],\"!defaultVRE\":\"projectb\"},"
        + "\"testconcretedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\",\"projecta\"]}],\"!defaultVRE\":\"projectb\"},"
        + "\"_id\":null,\"^pid\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);

    JsonNode t = mapper.readTree(testStr);
    ProjectAGeneralTestDoc x = new ProjectAGeneralTestDoc();
    x.name = "x";
    x.generalTestDocValue = "stuff";
    x.projectAGeneralTestDocValue = "other stuff";

    JsonNode allVariations = inducer.induce(x, ProjectAGeneralTestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceNoopExisting() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\",\"other\"]}],\"!defaultVRE\":\"projectb\"}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
        + "\"other\": {\"blub\": \"otherstuff\"}," + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    String testStr = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\",\"other\"]}],\"!defaultVRE\":\"projectb\"}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
        + "\"other\": {\"blub\": \"otherstuff\"}," + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    JsonNode t = mapper.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceSwitchToCorrectValueBeforeExisting() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"another\"]}, {\"v\":\"b\", \"a\":[\"projectb\", \"other\"]}],\"!defaultVRE\":\"another\"}, "
        + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
        + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    String testStr = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"another\",\"projectb\"]}, {\"v\":\"b\", \"a\":[\"other\"]}],\"!defaultVRE\":\"another\"}, "
        + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
        + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    JsonNode t = mapper.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceSwitchToCorrectValueAfterExisting() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\",\"other\"]}, {\"v\":\"x\", \"a\":[\"another\"]}],\"!defaultVRE\":\"projectb\"}, "
        + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
        + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    String testStr = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"other\"]}, {\"v\":\"x\", \"a\":[\"another\", \"projectb\"]}],\"!defaultVRE\":\"projectb\"}, "
        + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
        + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    JsonNode t = mapper.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceSwitchToCorrectValueAfterExistingWithRemove() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}, {\"v\":\"x\", \"a\":[\"other\"]}],\"!defaultVRE\":\"projectb\"},"
        + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
        + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    String testStr = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\", \"projectb\"]}],\"!defaultVRE\":\"projectb\"}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
        + "\"other\": {\"blub\": \"otherstuff\"}," + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    JsonNode t = mapper.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInduceNullCls() throws IOException {

    TestDoc x = new TestDoc();
    inducer.induce(x, null);
  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectObject() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}]}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
        + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    TestDoc x = new TestDoc();
    x.setId("TST002");
    x.name = "x";
    x.blah = "stuff";
    inducer.induce(x, TestDoc.class, existing);
  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesNameIsInt() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\": 42}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
        + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesNameIsArray() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[42]}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
        + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);

    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesNameIsNullArray() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[null]}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
        + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesNameIsNull() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":null}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
        + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesBaseDocIsNotAnObject() throws IOException {

    String inTree = "{\"testinheritsfromtestbasedoc\": 42, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
        + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesTestDocIsNotAnObject() throws IOException {

    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"a\", \"a\":[\"projectb\"]}]}, " + "\"projectb-testdoc\": 42, "
        + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesNameIsNullArrayTestDocIsNull() throws IOException {
    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"a\", \"a\":[\"projectb\"]}]}, " + "\"projectb-testdoc\": null, "
        + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesTestDocIsEmpty() throws IOException {

    String inTree = "{\"testinheritsfromtestbasedoc\":{\"name\":[{\"v\":\"a\"}]}, " + "\"projectb-testdoc\": {}, "
        + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) mapper.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  private TestDoc createTestDoc() {
    TestDoc x = new TestDoc();
    x.setId("TST001");
    x.name = "x";
    x.blah = "stuff";
    return x;
  }
}
