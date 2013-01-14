package nl.knaw.huygens.repository.variation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class VariationInducerTest {

  private ObjectMapper m;

  @Before
  public void setUp() throws Exception {
    m = new ObjectMapper();
  }

  @Test
  public void testInduce() throws IOException {
    String testStr = "{\"common\":{\"a\":[{\"v\":\"a\", \"a\":[\"variation\"]}]}, \"variation\": {\"blah\": \"stuff\"}, \"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.a = "a";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class);
    assertEquals(t, allVariations);
  }
  
  @Test
  public void testInduceExisting() throws IOException {
    String inTree = "{\"common\":{\"a\":[{\"v\":\"b\", \"a\":[\"other\"]}]}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"a\":[{\"v\":\"b\", \"a\":[\"other\"]}, {\"v\":\"a\", \"a\":[\"variation\"]}]}, " +
                     "\"variation\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.a = "a";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceCorrectExisting() throws IOException {
    String inTree = "{\"common\":{\"a\":[{\"v\":\"a\", \"a\":[\"other\"]}]}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"a\":[{\"v\":\"a\", \"a\":[\"other\", \"variation\"]}]}, " +
                     "\"variation\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.a = "a";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }

  @Test
  public void testInduceNoopExisting() throws IOException {
    String inTree = "{\"common\":{\"a\":[{\"v\":\"a\", \"a\":[\"variation\", \"other\"]}]}, " +
                    "\"variation\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"a\":[{\"v\":\"a\", \"a\":[\"variation\", \"other\"]}]}, " +
                     "\"variation\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.a = "a";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }
  
  @Test
  public void testInduceSwitchToCorrectValueBeforeExisting() throws IOException {
    String inTree = "{\"common\":{\"a\":[{\"v\":\"a\", \"a\":[\"another\"]}, {\"v\":\"b\", \"a\":[\"variation\", \"other\"]}]}, " +
                    "\"variation\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"a\":[{\"v\":\"a\", \"a\":[\"another\", \"variation\"]}, {\"v\":\"b\", \"a\":[\"other\"]}]}, " +
                     "\"variation\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.a = "a";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }
  
  @Test
  public void testInduceSwitchToCorrectValueAfterExisting() throws IOException {
    String inTree = "{\"common\":{\"a\":[{\"v\":\"b\", \"a\":[\"variation\", \"other\"]}, {\"v\":\"a\", \"a\":[\"another\"]}]}, " +
                    "\"variation\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"a\":[{\"v\":\"b\", \"a\":[\"other\"]}, {\"v\":\"a\", \"a\":[\"another\", \"variation\"]}]}, " +
                     "\"variation\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.a = "a";
    x.blah = "stuff";
    JsonNode allVariations = inducer.induce(x, TestDoc.class, existing);
    assertEquals(t, allVariations);
  }
  
  @Test
  public void testInduceSwitchToCorrectValueAfterExistingWithRemove() throws IOException {
    String inTree = "{\"common\":{\"a\":[{\"v\":\"b\", \"a\":[\"variation\"]}, {\"v\":\"a\", \"a\":[\"other\"]}]}, " +
                    "\"variation\": {\"blah\": \"stuff\"}, " +
                    "\"other\": {\"blub\": \"otherstuff\"}," +
                    "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    String testStr = "{\"common\":{\"a\":[{\"v\":\"a\", \"a\":[\"other\", \"variation\"]}]}, " +
                     "\"variation\": {\"blah\": \"stuff\"}, " +
                     "\"other\": {\"blub\": \"otherstuff\"}," +
                     "\"^type\":null,\"_id\":null,\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    VariationInducer inducer = new VariationInducer();
    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    JsonNode t = m.readTree(testStr);
    TestDoc x = new TestDoc();
    x.a = "a";
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
    String inTree = "{\"common\":{\"a\":[{\"v\":\"b\", \"a\":[\"variation\"]}]}, " +
                    "\"variation\": {\"blah\": \"stuff\"}, " +
                    "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    VariationInducer inducer = new VariationInducer();
    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    TestDoc x = new TestDoc();
    x.setId("TST002");
    x.a = "a";
    x.blah = "stuff";
    inducer.induce(x, TestDoc.class, existing);
  }
  
  @Test
  public void testInduceIncorrectValues() throws IOException {
    String inTree = "{\"common\":{\"a\": 42}, " +
                    "\"variation\": {\"blah\": \"stuff\"}, " +
                    "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    VariationInducer inducer = new VariationInducer();
    ObjectNode existing = (ObjectNode) m.readTree(inTree);
    TestDoc x = new TestDoc();
    x.setId("TST001");
    x.a = "a";
    x.blah = "stuff";
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    inTree = "{\"common\":{\"a\":[42]}, " +
             "\"variation\": {\"blah\": \"stuff\"}, " +
             "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";
    
    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    inTree = "{\"common\":{\"a\":[null]}, " +
        "\"variation\": {\"blah\": \"stuff\"}, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }

    inTree = "{\"common\":{\"a\":null}, " +
        "\"variation\": {\"blah\": \"stuff\"}, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    inTree = "{\"common\": 42, " +
        "\"variation\": {\"blah\": \"stuff\"}, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    inTree = "{" +
        "\"variation\": {\"blah\": \"stuff\"}, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    inTree = "{\"common\":{\"a\":[{\"v\":\"a\", \"a\":[\"variation\"]}]}, " +
        "\"variation\": 42, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    
    inTree = "{\"common\":{\"a\":[{\"v\":\"a\", \"a\":[\"variation\"]}]}, " +
        "\"variation\": null, " +
        "\"^type\":null,\"_id\": \"TST001\",\"^rev\":0,\"^lastChange\":null,\"^creation\":null,\"^deleted\":false}";

    existing = (ObjectNode) m.readTree(inTree);
    try {
      inducer.induce(x, TestDoc.class, existing);
      fail();
    } catch (VariationException e) {
      // Expected
    }
    
    inTree = "{\"common\":{\"a\":[{\"v\":\"a\"}]}, " +
        "\"variation\": {}, " +
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
