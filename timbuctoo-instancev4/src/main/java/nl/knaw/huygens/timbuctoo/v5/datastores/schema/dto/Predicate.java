package nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(value = { "optional" }, allowGetters = true)
public class Predicate {
  private String name;
  private final Direction direction;
  private Type owner;
  private Set<String> valueTypes = new HashSet<>(10);
  private Set<String> referenceTypes = new HashSet<>(10);
  private long occurrences = 0;
  private boolean list;

  @JsonCreator
  public Predicate(@JsonProperty("name") String name, @JsonProperty("direction") Direction direction) {
    this.name = name;
    this.direction = direction;
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

  public void incUsage(long occurrences) {
    this.occurrences += occurrences;
    owner.setOccurrences(this.occurrences);
  }

  public boolean isOptional() {
    return occurrences < owner.getOccurrences();
  }

  public long getOccurrences() {
    return occurrences;
  }

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

  public boolean isList() {
    return list;
  }

  public void setList(boolean list) {
    this.list = list;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Predicate other = (Predicate) obj;

    return Objects.equal(this.name, other.name) &&
      Objects.equal(this.direction, other.direction) &&
      Objects.equal(this.valueTypes, other.valueTypes) &&
      Objects.equal(this.referenceTypes, other.referenceTypes);
  }

  public Direction getDirection() {
    return direction;
  }
}
