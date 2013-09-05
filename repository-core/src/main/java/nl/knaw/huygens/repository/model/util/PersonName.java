package nl.knaw.huygens.repository.model.util;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.repository.model.util.PersonNameComponent.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

/**
 * A person name consisting of components (the order is important).
 * Based on the TEI description of person names.
 */
public class PersonName {

  private static final Set<Type> ALL = EnumSet.allOf(Type.class);
  private static final Set<Type> SHORT = EnumSet.of(Type.FORENAME, Type.SURNAME, Type.NAME_LINK);

  private List<PersonNameComponent> components;

  public PersonName() {
    components = Lists.newArrayList();
  }

  public List<PersonNameComponent> getComponents() {
    return components;
  }

  public void setComponents(List<PersonNameComponent> components) {
    this.components = components;
  }

  public void addNameComponent(Type type, String value) {
    if (value != null && value.length() > 0) {
      components.add(new PersonNameComponent(type, value));
    }
  }

  @JsonIgnore
  public String getFullName() {
    PersonNameBuilder builder = new PersonNameBuilder();
    appendTo(builder, ALL);
    return builder.getName();
  }

  @JsonIgnore
  public String getShortName() {
    PersonNameBuilder builder = new PersonNameBuilder();
    appendTo(builder, SHORT);
    return builder.getName();
  }

  @JsonIgnore
  public String getSortName() {
    PersonNameBuilder builder = new PersonNameBuilder();
    int surnames = appendTo(builder, EnumSet.of(Type.SURNAME));
    appendForenames(builder);
    appendTo(builder, EnumSet.of(Type.NAME_LINK));
    if (surnames == 0) {
      appendTo(builder, EnumSet.of(Type.ADD_NAME));
    }
    return builder.getName();
  }

  private int appendTo(PersonNameBuilder builder, Set<Type> types) {
    int count = 0;
    for (PersonNameComponent component : components) {
      if (types.contains(component.getType())) {
        builder.addComponent(component);
        count++;
      }
    }
    return count;
  }

  private void appendForenames(PersonNameBuilder builder) {
    Type prev = null;
    for (PersonNameComponent component : components) {
      Type type = component.getType();
      if (type == Type.FORENAME || (type == Type.GEN_NAME && prev == Type.FORENAME)) {
        builder.addComponent(component);
      }
      prev = type;
    }
  }

  @Override
  public String toString() {
    return getFullName();
  }

}
