package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

@Value.Immutable
public interface Metadata {
  @JsonCreator
  static Metadata create(
      @JsonProperty("rdfType") Optional<String> rdfType,
      @JsonProperty("props") Map<String, MetadataProp> props) {
    return ImmutableMetadata.builder().rdfType(rdfType).props(props).build();
  }

  Optional<String> getRdfType();

  Map<String, MetadataProp> getProps();
}
