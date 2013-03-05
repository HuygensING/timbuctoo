package nl.knaw.huygens.repository.variation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.knaw.huygens.repository.variation.model.projectb.TestDoc;

public class VariationInducerTest {

  private ObjectMapper m;

  @Before
  public void setUp() throws Exception {
    m = new ObjectMapper();
  }

  @Test
  public void testInduce() throws IOException {
    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\"]}]}, " +
                     "\"projectb-testdoc\": {\"blah\": \"stuff\"},\"_id\":null," +
                     "\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.name = "x";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class);
    assertEquals(t, allVariations);
  }
  
  @Test
  public void testInduceExisting() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"other\"]}]}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"other\"]}, {\"v\":\"x\", \"a\":[\"projectb\"]}]}, " +
                     "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
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
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\"]}]}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\", \"projectb\"]}]}, " +
                     "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
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
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\", \"other\"]}]}, " +
                    "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\", \"other\"]}]}, " +
                     "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
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
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"another\"]}, {\"v\":\"b\", \"a\":[\"projectb\", \"other\"]}]}, " +
                    "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"another\", \"projectb\"]}, {\"v\":\"b\", \"a\":[\"other\"]}]}, " +
                     "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
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
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\", \"other\"]}, {\"v\":\"x\", \"a\":[\"another\"]}]}, " +
                    "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"other\"]}, {\"v\":\"x\", \"a\":[\"another\", \"projectb\"]}]}, " +
                     "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
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
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}, {\"v\":\"x\", \"a\":[\"other\"]}]}, " +
                    "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"testbasedoc\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\", \"projectb\"]}]}, " +
                     "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
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
    VariationInducer inducer = new VariationInducer();
    TestDoc x = new TestDoc();
    inducer.induce(x, null);
  }
  
  @Test(expected = VariationException.class)
  public void testInduceIncorrectObject() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}]}, " +
                    "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                    "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    VariationInducer inducer = new VariationInducer();
    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    TestDoc x = new TestDoc();
    x.setId("TST002");
    x.name = "x";
    x.blah = "stuff";
    inducer.induce(x, TestDoc.class, existing);
  }
  
  @Test
  public void testInduceIncorrectValues() throws IOException {
    String inTree = "{\"testbasedoc\":{\"name\": 42}, " +
                    "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
                    "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    VariationInducer inducer = new VariationInducer();
    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    TestDoc x = new TestDoc();
    x.setId("TST001");
    x.name = "x";
    x.blah = "stuff";
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    inTree = "{\"testbasedoc\":{\"name\":[42]}, " +
             "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
             "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    
    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    inTree = "{\"testbasedoc\":{\"name\":[null]}, " +
        "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
        "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }

    inTree = "{\"testbasedoc\":{\"name\":null}, " +
        "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
        "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    inTree = "{\"testbasedoc\": 42, " +
        "\"projectb-testdoc\": {\"blah\": \"stuff\"}, " +
        "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"a\", \"a\":[\"projectb\"]}]}, " +
        "\"projectb-testdoc\": 42, " +
        "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    
    inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"a\", \"a\":[\"projectb\"]}]}, " +
        "\"projectb-testdoc\": null, " +
        "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    inTree = "{\"testbasedoc\":{\"name\":[{\"v\":\"a\"}]}, " +
        "\"projectb-testdoc\": {}, " +
        "\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
  }
}
