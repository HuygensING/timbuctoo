package nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(value = { "optional" }, allowGetters = true)
public class Predicate {
  private String name;
  private Type owner;
  private Set<String> valueTypes = new HashSet<>(10);
  private Set<String> referenceTypes = new HashSet<>(10);
  private long occurrences = 0;
  private boolean list;

  @JsonCreator
  public Predicate(@JsonProperty("name") String name) {
    this.name = name;
  }

  public Set<String> getReferenceTypes() {
    return referenceTypes;
  }

  public Set<String> getValueTypes() {
    return valueTypes;
  }

  public void addValueType(String valueType) {
    this.valueTypes.add(valueType);
  }

  public void addReferenceType(String valueType) {
    this.referenceTypes.add(valueType);
  }

  public void incUsage() {
    occurrences++;
    owner.setOccurrences(occurrences);
  }

  public boolean isOptional() {
    return occurrences < owner.getOccurrences();
  }

  public long getOccurrences() {
    return occurrences;
  }

  @JsonIgnore
  public String getName() {
    return name;
  }

  protected void setValueTypes(Set<String> valueTypes) {
    this.valueTypes = valueTypes;
  }

  public void setReferenceTypes(Set<String> referenceTypes) {
    this.referenceTypes = referenceTypes;
  }

  public void setOwner(Type owner) {
    this.owner = owner;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isList() {
    return list;
  }

  public void setList(boolean list) {
    this.list = list;
  }
}
