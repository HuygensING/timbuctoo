package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.immutables.value.Value;

@Value.Immutable
@JsonTypeName("UriMetadataProp")
public interface UriMetadataProp extends MetadataProp {
  @JsonCreator
  static UriMetadataProp create(@JsonProperty(value = "predicate", required = true) String predicate) {
    return ImmutableUriMetadataProp.builder().predicate(predicate).build();
  }
}
