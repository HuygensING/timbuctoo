package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

public class BoundSubject {

  private final String value;
  private final Set<String> types;

  public BoundSubject(String value) {
    this.value = value;
    types = new HashSet<>();
  }

  public BoundSubject(String value, String type) {
    this.value = value;
    types = Sets.newHashSet(type);
  }

  public BoundSubject(String value, Set<String> types) {
    this.value = value;
    this.types = types;
  }

  public String getValue() {
    return value;
  }

  public Set<String> getType() {
    return types;
  }

  @Override
  public String toString() {
    return "BoundSubject{" +
      "value='" + value + '\'' +
      ", types=" + types +
      '}';
  }
}
