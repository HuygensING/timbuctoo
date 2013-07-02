package nl.knaw.huygens.repository.model.util;

import java.util.Map;

import com.google.common.collect.Maps;

public enum PersonNameComponentType {
  SURNAME("surname"), //
  FORENAME("forename"), //
  ROLE_NAME("roleName"), //
  ADD_NAME("addName"), //
  NAME_LINK("nameLink"), //
  GEN_NAME("genName");

  private static final Map<String, PersonNameComponentType> MAP = createMap();

  private static Map<String, PersonNameComponentType> createMap() {
    Map<String, PersonNameComponentType> map = Maps.newHashMap();
    for (PersonNameComponentType type : PersonNameComponentType.values()) {
      map.put(type.getName(), type);
    }
    return map;
  }

  public static PersonNameComponentType getInstance(String name) {
    return MAP.get(name);
  }

  private final String name;

  private PersonNameComponentType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
