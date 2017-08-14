package nl.knaw.huygens.timbuctoo.v5.archetypes.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReference.class)
@JsonDeserialize(as = ImmutableReference.class)
public interface Reference {

  String getTargetArchetype();

  Direction getDirection();
}
