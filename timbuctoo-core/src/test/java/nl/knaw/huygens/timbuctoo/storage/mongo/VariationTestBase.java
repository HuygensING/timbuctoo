package nl.knaw.huygens.timbuctoo.storage.mongo;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projectb.ProjectBDomainEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

public abstract class VariationTestBase {

  protected ObjectNode createSystemObjectNode(String id, String name, String testValue1, String testValue2) {
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

  protected Map<String, Object> newBaseDomainEntityMap(String id, String pid, String value) {
    Map<String, Object> map = newDefaultMap(id, pid);
    map.put(propertyName(BaseDomainEntity.class, "generalTestDocValue"), value);
    return map;
  }

  protected Map<String, Object> newDefaultMap(String id, String pid) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("_id", id);
    map.put("^rev", 0);
    map.put(DomainEntity.PID, pid);
    map.put(DomainEntity.DELETED, false);
    return map;
  }

  protected ProjectBDomainEntity newProjectBDomainEntity(String id, String pid, String projectBGeneralTestDocValue) {
    ProjectBDomainEntity entity = new ProjectBDomainEntity(id);
    entity.projectBGeneralTestDocValue = projectBGeneralTestDocValue;
    entity.setPid(pid);
    return entity;
  }

}
