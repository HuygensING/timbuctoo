package nl.knaw.huygens.timbuctoo.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Value.Immutable
@JsonSerialize(as = ImmutableUser.class)
@JsonDeserialize(as = ImmutableUser.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface User {
  static User create(String displayName, String persistentId, String apiKey, Map<String, String> properties) {
    return ImmutableUser.builder()
        .displayName(displayName)
        .persistentId(persistentId)
        .apiKey(apiKey)
        .properties(properties)
        .build();
  }

  static User create(String displayName, String persistentId, Map<String, String> properties) {
    return create(displayName, persistentId, null, properties);
  }

  static User create(String displayName, String persistentId) {
    return create(displayName, persistentId, new HashMap<>());
  }

  @Nullable
  String getDisplayName();

  @Nullable
  String getPersistentId();

  @Nullable
  String getApiKey();

  @Nullable
  @Value.Default
  default Map<String, String> getProperties() {
    return new HashMap<>();
  }
}
