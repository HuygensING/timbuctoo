package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javaslang.collection.Array;
import org.immutables.value.Value;

import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Immutable
public interface JsonLdImport {

  Entity[] getGenerates();

  @JsonCreator
  static JsonLdImport create(@JsonProperty("prov:generates") Entity[] generates) {
    return ImmutableJsonLdImport
      .builder()
      .generates(generates)
      .build();
  }

}
