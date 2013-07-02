package nl.knaw.huygens.repository.model.util;

import org.apache.commons.lang.StringUtils;

/**
 * A component of a person name.
 * The names used are TEI-element names.
 */
public class PersonNameComponent {

  private PersonNameComponentType type;
  private String value;

  public PersonNameComponent() {}

  public PersonNameComponent(PersonNameComponentType type, String value) {
    setType(type);
    setValue(value);
  }

  public PersonNameComponentType getType() {
    return type;
  }

  public void setType(PersonNameComponentType type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = StringUtils.stripToEmpty(value);
  }

}
