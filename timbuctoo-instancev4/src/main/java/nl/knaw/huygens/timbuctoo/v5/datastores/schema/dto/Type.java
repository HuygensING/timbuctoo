package nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Type {
  private String name;
  private Map<String, Predicate> predicates;
  private long occurrences = 0;
  private final Function<String, Predicate> predicateMaker = (name) -> {
    Predicate predicate = new Predicate(name);
    predicate.setOwner(this);
    return predicate;
  };

  public Map<String, Predicate> getPredicates() {
    return predicates;
  }

  public void setPredicates(Map<String, Predicate> predicates) {
    this.predicates = predicates;
    predicates.forEach((name, predicate) -> {
      predicate.setName(name);
      predicate.setOwner(this);
    });
  }

  @JsonCreator
  public Type(@JsonProperty("name") String name) {
    this.name = name;
    predicates = new HashMap<>(64);
  }

  public Predicate getOrCreatePredicate(String name) {
    return predicates.computeIfAbsent(name, predicateMaker);
  }

  public void setOccurrences(long occurrences) {
    if (this.occurrences < occurrences) {
      this.occurrences = occurrences;
    }
  }

  public long getOccurrences() {
    return occurrences;
  }

  @JsonIgnore
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
