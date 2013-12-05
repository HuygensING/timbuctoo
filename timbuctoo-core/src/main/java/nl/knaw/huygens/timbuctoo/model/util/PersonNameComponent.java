package nl.knaw.huygens.timbuctoo.model.util;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

/**
 * A component of a person name.
 * The names used are TEI-element names.
 */
public class PersonNameComponent {

  private Type type;
  private String value;

  public PersonNameComponent() {}

  public PersonNameComponent(Type type, String value) {
    setType(type);
    setValue(value);
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = StringUtils.stripToEmpty(value);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof PersonNameComponent) {
      PersonNameComponent that = (PersonNameComponent) object;
      return Objects.equal(this.type, that.type) && Objects.equal(this.value, that.value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(type, value);
  }

  // -------------------------------------------------------------------

  public static enum Type {
    SURNAME("surname"), //
    FORENAME("forename"), //
    ROLE_NAME("roleName"), //
    ADD_NAME("addName"), //
    NAME_LINK("nameLink"), //
    GEN_NAME("genName");

    private static final Map<String, Type> MAP = createMap();

    private static Map<String, Type> createMap() {
      Map<String, Type> map = Maps.newHashMap();
      for (Type type : Type.values()) {
        map.put(type.getName(), type);
      }
      return map;
    }

    public static Type getInstance(String name) {
      return MAP.get(name);
    }

    private final String name;

    private Type(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

  }

}
