package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

public abstract class VariationTestBase {

  protected ObjectNode createSystemObjectNode(String id, String name, String testValue1, String testValue2) throws IOException, JsonProcessingException {
    Map<String, Object> map = Maps.newHashMap();
    addNonNullValueToMap(map, "_id", id);
    addNonNullValueToMap(map, "testsystementity.name", name);
    addNonNullValueToMap(map, "testsystementity.testValue1", testValue1);
    addNonNullValueToMap(map, "testsystementity.testValue2", testValue2);
    map.put("^rev", 0);
    map.put("_deleted", false);

    return getMapper().valueToTree(map);
  }

  protected abstract ObjectMapper getMapper();

  protected void addNonNullValueToMap(Map<String, Object> map, String key, String value) {
    if (value != null) {
      map.put(key, value);
    }
  }

  protected Map<String, Object> createGeneralTestDocMap(String id, String pid, String generalTestDocValue) {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.put("_id", id);
    expectedMap.put("^rev", 0);
    expectedMap.put("_deleted", false);
    expectedMap.put("^pid", pid);
    expectedMap.put("generaltestdoc.generalTestDocValue", generalTestDocValue);
  
    return expectedMap;
  }

}