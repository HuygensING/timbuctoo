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

  /*
  @JsonCreator
  static Entity create(@JsonProperty("@type") String entityType,
                       @JsonProperty("prov:specializationOf") URI specializationOf,
                       @JsonProperty("prov:wasRevisionOf") HashMap<String,String> revisionOf,
                       @JsonProperty("tim:additions") HashMap<String, String> additions,
                       @JsonProperty("tim:deletions") HashMap<String, String> deletions,
                       @JsonProperty("tim:replacements") HashMap<String, String> replacements) {
    return ImmutableEntity.builder()
                          .entityType(entityType)
                          .specializationOf(specializationOf)
                          .wasRevisionOf(revisionOf)
                          .additions(additions)
                          .deletions(deletions)
                          .replacements(replacements)
                          .build();
  }
  */

  String getEntityType();

  URI getSpecializationOf();

  Map<String,String> getWasRevisionOf();

  Map<String, String> getAdditions();

  Map<String, String> getDeletions();

  Map<String, String> getReplacements();
}
