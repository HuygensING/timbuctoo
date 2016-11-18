package nl.knaw.huygens.timbuctoo.database.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityLookup.class)
@JsonDeserialize(as = ImmutableEntityLookup.class)
public interface EntityLookup {

  int getRev();

  String getCollection();

  UUID getTimId();
}
