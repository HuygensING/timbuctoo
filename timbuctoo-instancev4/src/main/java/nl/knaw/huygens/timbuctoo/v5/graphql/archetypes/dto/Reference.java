package nl.knaw.huygens.timbuctoo.v5.graphql.archetypes.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReference.class)
@JsonDeserialize(as = ImmutableReference.class)
public interface Reference {

  String getTargetArchetype();

  Direction getDirection();
}
