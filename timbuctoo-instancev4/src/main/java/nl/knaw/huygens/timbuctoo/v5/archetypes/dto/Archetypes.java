package nl.knaw.huygens.timbuctoo.v5.archetypes.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dropwizard.validation.ValidationMethod;
import org.immutables.value.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableArchetypes.class)
@JsonDeserialize(as = ImmutableArchetypes.class)
public abstract class Archetypes {
  public abstract List<Archetype> getArchetypes();

  @ValidationMethod
  @JsonIgnore
  public boolean uniqueArchetypeNames() {
    Set<String> archetypeNames = new HashSet<>();
    for (Archetype archetype : getArchetypes()) {
      if (archetypeNames.contains(archetype.getName())) {
        return false;
      } else {
        archetypeNames.add(archetype.getName());
      }
    }
    return true;
  }

  public static Archetypes create() {
    return ImmutableArchetypes.builder().build();
  }

}
