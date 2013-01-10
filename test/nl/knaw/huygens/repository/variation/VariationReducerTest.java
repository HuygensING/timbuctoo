package nl.knaw.huygens.repository.variation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.mongo.MongoDiff;
import nl.knaw.huygens.repository.variation.VariationReducer.VariationException;

public class VariationReducerTest {
  public static class X extends Document {
    public X() { super(); }

    public String a;
    public String blah;
    @Override
    public String getDescription() {
      return null;
    }
    @Override
    public void fetchAll(Storage storage) {
    }
  }

  private VariationReducer reducer;
  
  @Before
  public void setUp() {
    reducer = new VariationReducer();
  }
  
  @Test
  public void testReduce() throws IOException {
    String x = "{\"common\":{\"a\":[{\"v\":\"a\", \"agreed\":[\"variation\"]}]}, \"variation\": {\"blah\": \"stuff\"}}";
    ObjectMapper m = new ObjectMapper();
    JsonNode t = m.readTree(x);
    X val = reducer.reduce(t, X.class);
    X testVal = new X();
    testVal.a = "a";
    testVal.blah = "stuff"; 
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }
  
  @Test
  public void testReduceSpecificDataOnly() throws IOException {
    String x = "{\"variation\": {\"blah\": \"stuff\"}}";
    ObjectMapper m = new ObjectMapper();
    JsonNode t = m.readTree(x);
    X val = reducer.reduce(t, X.class);
    X testVal = new X();
    testVal.blah = "stuff"; 
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }
  
  @Test
  public void testReduceCommonDataOnly() throws IOException {
    String x = "{\"common\":{\"a\":[{\"v\":\"b\", \"agreed\":[\"blub\"]}, {\"v\":\"a\", \"agreed\":[\"variation\"]}]}}";
    ObjectMapper m = new ObjectMapper();
    JsonNode t = m.readTree(x);
    X val = reducer.reduce(t, X.class);
    X testVal = new X();
    testVal.a = "a";
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

  @Test
  public void testReduceMissingVariation() throws IOException {
    String x = "{\"common\":{\"a\":[{\"v\":\"b\", \"agreed\":[\"blub\"]}]}}";
    ObjectMapper m = new ObjectMapper();
    JsonNode t = m.readTree(x);
    X val = reducer.reduce(t, X.class);
    X testVal = new X();
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }
  
  @Test(expected = VariationException.class)
  public void testReduceVariationNonObject() throws IOException {
    String x = "{\"variation\": \"flups\"}";
    ObjectMapper m = new ObjectMapper();
    JsonNode t = m.readTree(x);
    
    reducer.reduce(t, X.class); // This will throw
  }
  
  
  @Test(expected = VariationException.class)
  public void testReduceMalformedCommonItem() throws IOException {
    String x = "{\"common\":{\"a\": 42}}";
    ObjectMapper m = new ObjectMapper();
    JsonNode t = m.readTree(x);
    
    reducer.reduce(t, X.class); // This will throw
  }
  
  @Test(expected = VariationException.class)
  public void testReduceMalformedCommonValueArrayItem() throws IOException {
    String x = "{\"common\":{\"a\":[42]}}";
    ObjectMapper m = new ObjectMapper();
    JsonNode t = m.readTree(x);
    
    reducer.reduce(t, X.class); // This will throw
  }
  
  @Test(expected = VariationException.class)
  public void testReduceMalformedCommonValueArrayItemAgreed() throws IOException {
    String x = "{\"common\":{\"a\":[{\"v\":\"b\", \"agreed\":42}]}}";
    ObjectMapper m = new ObjectMapper();
    JsonNode t = m.readTree(x);
    
    reducer.reduce(t, X.class); // This will throw
  }
}
