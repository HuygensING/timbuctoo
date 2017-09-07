package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.knaw.huygens.timbuctoo.v5.archetypes.dto.ImmutableArchetype;
import org.immutables.value.Value;



@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Immutable
@JsonSerialize(as = ImmutableJsonLdImport.class)
@JsonDeserialize(as = ImmutableJsonLdImport.class)
public interface JsonLdImport {

  @JsonProperty("prov:generates")
  Entity[] getGenerates();

  @JsonCreator
  static JsonLdImport create(@JsonProperty("prov:generates") Entity[] generates) {
    return ImmutableJsonLdImport
      .builder()
      .generates(generates)
      .build();
  }

}
