package nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(value = {"optional"}, allowGetters = true)
public class Predicate {
  private final Direction direction;
  private String name;
  private Type owner;
  private Map<String, Long> valueTypes = new HashMap<>(10);
  private Map<String, Long> referenceTypes = new HashMap<>(10);
  private long subjectsWithThisPredicate = 0;
  private long subjectsWithThisPredicateAsList = 0;

  private boolean hasBeenList = false;
  private boolean hasBeenSingular = false;
  private boolean isExplicit = false;

  @JsonCreator
  public Predicate(@JsonProperty("name") String name, @JsonProperty("direction") Direction direction) {
    this.name = name;
    this.direction = direction;
  }

  public Map<String, Long> getReferenceTypes() {
    return referenceTypes;
  }

  public void setReferenceTypes(Map<String, Long> referenceTypes) {
    this.referenceTypes = referenceTypes;
  }

  public Map<String, Long> getValueTypes() {
    return valueTypes;
  }

  public void setValueTypes(Map<String, Long> valueTypes) {
    this.valueTypes = valueTypes;
  }

  @JsonIgnore
  public Set<String> getUsedReferenceTypes() {
    return referenceTypes.keySet();
  }

  @JsonIgnore
  public Set<String> getUsedValueTypes() {
    return valueTypes.keySet();
  }

  public void registerSubject(long occurrences) {
    this.subjectsWithThisPredicate += occurrences;
  }

  public long getSubjectsWithThisPredicate() {
    return subjectsWithThisPredicate;
  }

  @JsonIgnore
  public boolean inUse() {
    return subjectsWithThisPredicate > 0;
  }

  @JsonIgnore
  public boolean isExplicit() {
    return isExplicit;
  }

  public String getName() {
    return name;
  }

  @JsonIgnore
  public Type getOwner() {
    return this.owner;
  }

  public void setOwner(Type owner) {
    this.owner = owner;
  }

  public long getSubjectsWithThisPredicateAsList() {
    return subjectsWithThisPredicateAsList;
  }

  public void setSubjectsWithThisPredicateAsList(long occurrences) {
    subjectsWithThisPredicateAsList = occurrences;
  }

  @JsonProperty("hasBeenList")
  public boolean hasBeenList() {
    return hasBeenList;
  }
  
  public void setIsExplicit(boolean isExplicit) {
    this.isExplicit = isExplicit;
  }

  @JsonProperty("hasBeenList")
  public void setHasBeenList(boolean hasBeenList) {
    this.hasBeenList = hasBeenList;
  }

  @JsonProperty("hasBeenSingular")
  public boolean isHasBeenSingular() {
    return hasBeenSingular;
  }

  @JsonProperty("hasBeenSingular")
  public void setHasBeenSingular(boolean hasBeenSingular) {
    this.hasBeenSingular = hasBeenSingular;
  }

  @JsonIgnore
  public boolean isList() {
    return subjectsWithThisPredicateAsList > 0;
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

  public void incValueType(String valueType, long mut) {
    this.valueTypes.compute(valueType, (type, cur) -> cur == null ? mut : cur + mut);
  }

  public void incReferenceType(String referenceType, long mut) {
    this.referenceTypes.compute(referenceType, (type, cur) -> cur == null ? mut : cur + mut);
  }

  public void registerListOccurrence(int mut) {
    this.subjectsWithThisPredicateAsList += mut;
  }

  public void finish() {
    this.hasBeenList = this.hasBeenList || subjectsWithThisPredicateAsList > 0;
    this.hasBeenSingular = this.hasBeenSingular || subjectsWithThisPredicateAsList <= 0;
  }
}
