package nl.knaw.huygens.timbuctoo.v5.permissions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.v5.permissions.satisfiable.Satisfiable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ConfigurablePermissionChecker implements PermissionChecker {
  private Set<String> satisfied = new HashSet<>();
  protected final ImmutableMap<String, Function<String, Boolean>> satisfiables;

  @JsonCreator
  public ConfigurablePermissionChecker(@JsonProperty("satisfiables") Map<String, Satisfiable> satisfiables) {
    this.satisfiables = ImmutableMap.copyOf(satisfiables);
  }

  @Override
  public void satisfy(String key, String value) {
    if (satisfiables.containsKey(key)) {
      if (satisfiables.get(key).apply(value)) {
        satisfied.add(key);
      }
    }
  }

  @Override
  public boolean hasPermission() {
    return satisfied.size() == satisfiables.keySet().size();
  }

  @Override
  public PermissionChecker split() {
    try {
      ConfigurablePermissionChecker clone = (ConfigurablePermissionChecker) super.clone();
      clone.satisfied = Sets.newHashSet(satisfied);
      return clone;
    } catch (CloneNotSupportedException e) {
      //direct subclass of object, so this exception is never thrown
      //stupid java
      throw new RuntimeException(e);
    }
  }
}
