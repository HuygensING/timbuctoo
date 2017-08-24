package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Immutable
@JsonDeserialize(as = ImmutableEntity.class)
@JsonSerialize(as = ImmutableEntity.class)
public interface Entity {
  String getEntityType();

  URI getSpecializationOf();

  Map<String, Object> getWasRevisionOf();

  Map<String, String[]> getAdditions();

  Map<String, Object> getDeletions();

  Map<String, Object> getReplacements();
}
