package nl.knaw.huygens.repository.variation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.knaw.huygens.repository.storage.mongo.MongoDiff;

public class VariationReducerTest {
  private VariationReducer reducer;
  private ObjectMapper m;

  @Before
  public void setUp() {
    m = new ObjectMapper();
    reducer = new VariationReducer();
  }
  
  @Test
  public void testReduce() throws IOException {
    String x = "{\"common\":{\"a\":[{\"v\":\"a\", \"a\":[\"variation\"]}]}, \"variation\": {\"blah\": \"stuff\"}}";
    JsonNode t = m.readTree(x);
    TestDoc val = reducer.reduce(t, TestDoc.class);
    TestDoc testVal = new TestDoc();
    testVal.a = "a";
    testVal.blah = "stuff"; 
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }
  
  @Test
  public void testReduceSpecificDataOnly() throws IOException {
    String x = "{\"variation\": {\"blah\": \"stuff\"}}";
    JsonNode t = m.readTree(x);
    TestDoc val = reducer.reduce(t, TestDoc.class);
    TestDoc testVal = new TestDoc();
    testVal.blah = "stuff"; 
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }
  
  @Test
  public void testReduceCommonDataOnly() throws IOException {
    String x = "{\"common\":{\"a\":[{\"v\":\"b\", \"a\":[\"blub\"]}, {\"v\":\"a\", \"a\":[\"variation\"]}]}}";
    JsonNode t = m.readTree(x);
    TestDoc val = reducer.reduce(t, TestDoc.class);
    TestDoc testVal = new TestDoc();
    testVal.a = "a";
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceMissingVariation() throws IOException {
    String x = "{\"common\":{\"a\":[{\"v\":\"b\", \"a\":[\"blub\"]}]}}";
    JsonNode t = m.readTree(x);
    TestDoc val = reducer.reduce(t, TestDoc.class);
    TestDoc testVal = new TestDoc();
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }
  
  @Test(expected = VariationException.class)
  public void testReduceVariationNonObject() throws IOException {
    String x = "{\"variation\": \"flups\"}";
    JsonNode t = m.readTree(x);
    
    reducer.reduce(t, TestDoc.class); // This will throw
  }
  
  
  @Test(expected = VariationException.class)
  public void testReduceMalformedCommonItem() throws IOException {
    String x = "{\"common\":{\"a\": 42}}";
    JsonNode t = m.readTree(x);
    
    reducer.reduce(t, TestDoc.class); // This will throw
  }
  
  @Test(expected = VariationException.class)
  public void testReduceMalformedCommonValueArrayItem() throws IOException {
    String x = "{\"common\":{\"a\":[42]}}";
    JsonNode t = m.readTree(x);
    
    reducer.reduce(t, TestDoc.class); // This will throw
  }
  
  @Test(expected = VariationException.class)
  public void testReduceMalformedCommonValueArrayItemAgreed() throws IOException {
    String x = "{\"common\":{\"a\":[{\"v\":\"b\", \"a\":42}]}}";
    JsonNode t = m.readTree(x);
    
    reducer.reduce(t, TestDoc.class); // This will throw
  }
}
