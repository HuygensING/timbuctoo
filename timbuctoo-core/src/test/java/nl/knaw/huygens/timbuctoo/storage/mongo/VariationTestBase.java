package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectAGeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.projectb.ProjectBGeneralTestDoc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class VariationTestBase {

  protected ObjectNode createSystemObjectNode(String id, String name, String testValue1, String testValue2) throws IOException, JsonProcessingException {
    Map<String, Object> map = Maps.newHashMap();
    addNonNullValueToMap(map, "_id", id);
    addNonNullValueToMap(map, "testsystementity.name", name);
    addNonNullValueToMap(map, "testsystementity.testValue1", testValue1);
    addNonNullValueToMap(map, "testsystementity.testValue2", testValue2);
    map.put("^rev", 0);
    map.put("^deleted", false);

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
    expectedMap.putAll(createDefaultMap(id, pid, expectedMap));
    expectedMap.put("generaltestdoc.generalTestDocValue", generalTestDocValue);

    return expectedMap;
  }

  protected Map<String, Object> createTestConcreteDocMap(String id, String pid, String name) {
    Map<String, Object> expectedMap = Maps.newHashMap();
    expectedMap.putAll(createDefaultMap(id, pid, expectedMap));
    expectedMap.put("testconcretedoc.name", name);

    return expectedMap;
  }

  protected Map<String, Object> createDefaultMap(String id, String pid, Map<String, Object> expectedMap) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("_id", id);
    map.put("^rev", 0);
    map.put("^deleted", false);
    map.put(DomainEntity.PID, pid);

    return map;
  }

  protected ProjectBGeneralTestDoc createProjectBGeneralTestDoc(String id, String pid, String projectBGeneralTestDocValue) {
    ProjectBGeneralTestDoc item = new ProjectBGeneralTestDoc();
    item.projectBGeneralTestDocValue = projectBGeneralTestDocValue;
    item.setVariations(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, id), new Reference(ProjectBGeneralTestDoc.class, id)));
    item.setCurrentVariation("projectb");
    item.setPid(pid);
    item.setId(id);
    return item;
  }

}