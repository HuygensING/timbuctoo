package nl.knaw.huygens.timbuctoo.v5.archetypes.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableArchetype.class)
@JsonDeserialize(as = ImmutableArchetype.class)
public interface Archetype {

  String getName();

  String getRequiredTypeUri();

  String getDescription();

  Set<PredicateDefinition> getPredicateDefinitions();

}
