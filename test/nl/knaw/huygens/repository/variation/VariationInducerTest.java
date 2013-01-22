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
    String testStr = "{\"common\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\"]}]}, \"projectb\": {\"blah\": \"stuff\"}, \"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
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
    String inTree = "{\"common\":{\"name\":[{\"v\":\"b\", \"a\":[\"other\"]}]}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"name\":[{\"v\":\"b\", \"a\":[\"other\"]}, {\"v\":\"x\", \"a\":[\"projectb\"]}]}, " +
                     "\"projectb\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
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
    String inTree = "{\"common\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\"]}]}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\", \"projectb\"]}]}, " +
                     "\"projectb\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
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
    String inTree = "{\"common\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\", \"other\"]}]}, " +
                    "\"projectb\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"name\":[{\"v\":\"x\", \"a\":[\"projectb\", \"other\"]}]}, " +
                     "\"projectb\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
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
    String inTree = "{\"common\":{\"name\":[{\"v\":\"x\", \"a\":[\"another\"]}, {\"v\":\"b\", \"a\":[\"projectb\", \"other\"]}]}, " +
                    "\"projectb\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"name\":[{\"v\":\"x\", \"a\":[\"another\", \"projectb\"]}, {\"v\":\"b\", \"a\":[\"other\"]}]}, " +
                     "\"projectb\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
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
    String inTree = "{\"common\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\", \"other\"]}, {\"v\":\"x\", \"a\":[\"another\"]}]}, " +
                    "\"projectb\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"name\":[{\"v\":\"b\", \"a\":[\"other\"]}, {\"v\":\"x\", \"a\":[\"another\", \"projectb\"]}]}, " +
                     "\"projectb\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
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
    String inTree = "{\"common\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}, {\"v\":\"x\", \"a\":[\"other\"]}]}, " +
                    "\"projectb\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"name\":[{\"v\":\"x\", \"a\":[\"other\", \"projectb\"]}]}, " +
                     "\"projectb\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
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
    String inTree = "{\"common\":{\"name\":[{\"v\":\"b\", \"a\":[\"projectb\"]}]}, " +
                    "\"projectb\": {\"blah\": \"stuff\"}, " +
                    "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

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
    String inTree = "{\"common\":{\"name\": 42}, " +
                    "\"projectb\": {\"blah\": \"stuff\"}, " +
                    "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

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
    inTree = "{\"common\":{\"name\":[42]}, " +
             "\"projectb\": {\"blah\": \"stuff\"}, " +
             "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    
    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    inTree = "{\"common\":{\"name\":[null]}, " +
        "\"projectb\": {\"blah\": \"stuff\"}, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }

    inTree = "{\"common\":{\"name\":null}, " +
        "\"projectb\": {\"blah\": \"stuff\"}, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    inTree = "{\"common\": 42, " +
        "\"projectb\": {\"blah\": \"stuff\"}, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    inTree = "{" +
        "\"projectb\": {\"blah\": \"stuff\"}, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    inTree = "{\"common\":{\"name\":[{\"v\":\"a\", \"a\":[\"projectb\"]}]}, " +
        "\"projectb\": 42, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    
    inTree = "{\"common\":{\"name\":[{\"v\":\"a\", \"a\":[\"projectb\"]}]}, " +
        "\"projectb\": null, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    inTree = "{\"common\":{\"name\":[{\"v\":\"a\"}]}, " +
        "\"projectb\": {}, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
  }
}
