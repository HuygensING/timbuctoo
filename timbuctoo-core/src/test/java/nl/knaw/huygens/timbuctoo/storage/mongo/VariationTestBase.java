package nl.knaw.huygens.timbuctoo.storage.mongo;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;

import java.io.IOException;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.variation.model.TestConcreteDoc;
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
    addNonNullValueToMap(map, propertyName("testsystementity", "name"), name);
    addNonNullValueToMap(map, propertyName("testsystementity", "testValue1"), testValue1);
    addNonNullValueToMap(map, propertyName("testsystementity", "testValue2"), testValue2);
    map.put("^rev", 0);
    return getMapper().valueToTree(map);
  }

  protected abstract ObjectMapper getMapper();

  protected void addNonNullValueToMap(Map<String, Object> map, String key, String value) {
    if (value != null) {
      map.put(key, value);
    }
  }

  protected Map<String, Object> createGeneralTestDocMap(String id, String pid, String generalTestDocValue) {
    Map<String, Object> map = Maps.newHashMap();
    map.putAll(createDefaultMap(id, pid, map));
    map.put(propertyName("generaltestdoc", "generalTestDocValue"), generalTestDocValue);
    return map;
  }

  protected Map<String, Object> createTestConcreteDocMap(String id, String pid, String name) {
    Map<String, Object> map = Maps.newHashMap();
    map.putAll(createDefaultMap(id, pid, map));
    map.put(propertyName(TestConcreteDoc.class, "name"), name);
    return map;
  }

  protected Map<String, Object> createDefaultMap(String id, String pid, Map<String, Object> expectedMap) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("_id", id);
    map.put("^rev", 0);
    map.put(DomainEntity.PID, pid);
    map.put(DomainEntity.DELETED, false);
    return map;
  }

  protected ProjectBGeneralTestDoc createProjectBGeneralTestDoc(String id, String pid, String projectBGeneralTestDocValue) {
    ProjectBGeneralTestDoc entity = new ProjectBGeneralTestDoc();
    entity.projectBGeneralTestDocValue = projectBGeneralTestDocValue;
    entity.setVariationRefs(Lists.newArrayList(new Reference(ProjectAGeneralTestDoc.class, id), new Reference(ProjectBGeneralTestDoc.class, id)));
    entity.setPid(pid);
    entity.setId(id);
    return entity;
  }

}
