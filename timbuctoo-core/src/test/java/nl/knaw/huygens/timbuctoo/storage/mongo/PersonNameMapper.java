package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PersonNameMapper {

  public static Object createPersonNameMap(PersonName personName) {
    Map<String, Object> personNameMap = Maps.newLinkedHashMap();
    List<Map<String, Object>> componentMapping = Lists.newArrayList();

    for (PersonNameComponent personNameComponent : personName.getComponents()) {
      componentMapping.add(createComponentMapping(personNameComponent));
    }

    personNameMap.put("components", componentMapping);

    return personNameMap;
  }

  private static Map<String, Object> createComponentMapping(PersonNameComponent personNameComponent) {
    Map<String, Object> nameComponent = Maps.newLinkedHashMap();

    nameComponent.put("type", personNameComponent.getType());
    nameComponent.put("value", personNameComponent.getValue());

    return nameComponent;
  }
}
