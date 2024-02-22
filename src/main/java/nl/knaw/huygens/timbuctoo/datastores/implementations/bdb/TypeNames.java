package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class TypeNames {
  @JsonProperty
  public final Map<String, String> prefixes = new HashMap<>();

  @JsonProperty
  public final Map<String, String> shorteneds = new HashMap<>();

  @JsonProperty
  public final Map<String, String> inverse = new HashMap<>();
}
