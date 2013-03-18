package nl.knaw.huygens.repository.variation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.knaw.huygens.repository.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.projectb.TestDoc;

public class VariationInducerTest {

  private ObjectMapper m;
  private VariationInducer inducer;

  @Before
  public void setUp() throws Exception {
    m = new ObjectMapper();
    inducer = new VariationInducer();
  }

  @After
  public void tearDown() {
    m = null;
    inducer = null;
  }

  @Test
  public void testInduce() throws IOException {
    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\",\"^default\"]}]},"
                     + "\"projectb-testdoc\": {\"blah\": \"stuff\"},\"_id\":null,\"^pid\":null,"
                     + "\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceWithSubModels() throws IOException {
    String testStr = "{\"projecta-projectageneraltestdoc\": {\"projectAGeneralTestDocValue\": \"other stuff\"},"
                     + "\"generaltestdoc\":{\"generalTestDocValue\":[{\"v\":\"stuff\", \"a\":[\"projecta\",\"^default\"]}]},"
                     + "\"testconcretedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projecta\",\"^default\"]}]},"
                     + "\"_id\":null,\"^pid\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    JsonNode t = m.readTree(testStr);
    ProjectAGeneralTestDoc x = new ProjectAGeneralTestDoc();
    x.name = "x";
    x.generalTestDocValue = "stuff";
    x.projectAGeneralTestDocValue = "other stuff";

    JsonNode allVariations = inducer.induce(x, ProjectAGeneralTestDoc.class);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceExisting() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"other\",\"^default\"]}]}, " + "\"other\": {\"blub\": \"otherstuff\"},"
                    + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"other\",\"^default\"]}, {\"v\":\"x\", \"a\":[\"projectb\"]}]}, "
                     + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
                     + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceCorrectExisting() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\",\"^default\"]}]}, " + "\"other\": {\"blub\": \"otherstuff\"},"
                    + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\",\"^default\",\"projectb\"]}]}, "
                     + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
                     + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceNoopExisting() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\",\"^default\",\"other\"]}]}, "
                    + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
                    + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\",\"^default\",\"other\"]}]}, "
                     + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
                     + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceSwitchToCorrectValueBeforeExisting() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"another\",\"^default\"]}, {\"v\":\"b\", \"a\":[\"projectb\", \"other\"]}]}, "
                    + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
                    + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"another\",\"^default\", \"projectb\"]}, {\"v\":\"b\", \"a\":[\"other\"]}]}, "
                     + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
                     + "\"other\": {\"blub\": \"otherstuff\"},"
                     + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceSwitchToCorrectValueAfterExisting() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\",\"^default\", \"other\"]}, {\"v\":\"x\", \"a\":[\"another\"]}]}, "
                    + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
                    + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"^default\",\"other\"]}, {\"v\":\"x\", \"a\":[\"another\", \"projectb\"]}]}, "
                     + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
                     + "\"other\": {\"blub\": \"otherstuff\"},"
                     + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceSwitchToCorrectValueAfterExistingWithRemove() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\",\"^default\"]}, {\"v\":\"x\", \"a\":[\"other\"]}]}, "
                    + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
                    + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\",\"^default\", \"projectb\"]}]}, "
                     + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " + "\"other\": {\"blub\": \"otherstuff\"},"
                     + "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false,\"^pid\":null}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
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
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}]}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
                    + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    TestDoc x = new TestDoc();
    x.setId("TST002");
    x.name = "x";
    x.blah = "stuff";
    inducer.induce(x, TestDoc.class, existing);
  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesNameIsInt() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\": 42}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
                    + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesNameIsArray() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":[42]}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
                    + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);

    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesNameIsNullArray() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":[null]}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
                    + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesNameIsNull() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":null}, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
                    + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesBaseDocIsNotAnObject() throws IOException {

    String inTree = "{\"testbasedoc\": 42, " + "\"projectb-testdoc\": {\"blah\": \"stuff\"}, "
                    + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesTestDocIsNotAnObject() throws IOException {

    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"a\", \"a\":[\"projectb\"]}]}, " + "\"projectb-testdoc\": 42, "
                    + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesNameIsNullArrayTestDocIsNull() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"a\", \"a\":[\"projectb\"]}]}, " + "\"projectb-testdoc\": null, "
                    + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    TestDoc x = createTestDoc();

    inducer.induce(x, TestDoc.class, existing);

  }

  @Test(expected = VariationException.class)
  public void testInduceIncorrectValuesTestDocIsEmpty() throws IOException {

    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"a\"}]}, " + "\"projectb-testdoc\": {}, "
                    + "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    ObjectNode existing = (ObjectNode) m.readTree(inTree);
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
