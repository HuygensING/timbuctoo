package nl.knaw.huygens.repository.variation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.mongo.MongoDiff;

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
  
  @Test
  public void testReduce() throws IOException {
    String x = "{\"common\":{\"a\":[{\"v\":\"a\", \"agreed\":[\"variation\"]}]}, \"variation\": {\"blah\": \"stuff\"}}";
    ObjectMapper m = new ObjectMapper();
    JsonNode t = m.readTree(x);
    X val = VariationReducer.reduce(t, X.class);
    X testVal = new X();
    testVal.a = "a";
    testVal.blah = "stuff"; 
    assertEquals(null, MongoDiff.diffDocuments(val, testVal));
  }

}
