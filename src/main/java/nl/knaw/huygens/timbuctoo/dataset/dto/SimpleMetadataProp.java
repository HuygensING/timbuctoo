package nl.knaw.huygens.timbuctoo.dataset.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonTypeName("SimpleMetadataProp")
public interface SimpleMetadataProp extends MetadataProp {
  static SimpleMetadataProp create(String predicate) {
    return create(predicate, null, null);
  }

  static SimpleMetadataProp create(String predicate, String valueType) {
    return create(predicate, valueType, null);
  }

  @JsonCreator
  static SimpleMetadataProp create(
      @JsonProperty(value = "predicate", required = true) String predicate,
      @JsonProperty("valueType") String valueType,
      @JsonProperty("language") String language) {
    if (language != null) {
      valueType = RdfConstants.LANGSTRING;
    } else if (valueType == null) {
      valueType = RdfConstants.STRING;
    }

    return ImmutableSimpleMetadataProp.builder()
        .predicate(predicate)
        .valueType(valueType)
        .language(Optional.ofNullable(language))
        .build();
  }

  String getValueType();

  Optional<String> getLanguage();
}
