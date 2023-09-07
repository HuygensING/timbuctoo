package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.knaw.huygens.timbuctoo.v5.redirectionservice.ImmutableEntityLookup;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityLookup.class)
@JsonDeserialize(as = ImmutableEntityLookup.class)
public interface EntityLookup {
  String getDataSetId();

  String getUri();

  User getUser();
}
