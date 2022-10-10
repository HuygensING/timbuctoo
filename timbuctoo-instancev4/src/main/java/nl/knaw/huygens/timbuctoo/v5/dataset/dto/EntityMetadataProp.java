package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
@JsonTypeName("EntityMetadataProp")
public interface EntityMetadataProp extends MetadataProp {
  @JsonCreator
  static EntityMetadataProp create(
      @JsonProperty(value = "predicate", required = true) String predicate,
      @JsonProperty(value = "useBaseUri") boolean useBaseUri,
      @JsonProperty(value = "entityUri", required = true) String entityUri,
      @JsonProperty(value = "properties", required = true) Map<String, MetadataProp> properties) {
    return ImmutableEntityMetadataProp.builder()
        .predicate(predicate)
        .useBaseUri(useBaseUri)
        .entityUri(entityUri)
        .properties(properties)
        .build();
  }

  default String getEntityUriFor(String baseUri) {
    if (getUseBaseUri()) {
      return baseUri + getEntityUri();
    }
    return getEntityUri();
  }

  boolean getUseBaseUri();

  String getEntityUri();

  Map<String, MetadataProp> getProperties();
}
