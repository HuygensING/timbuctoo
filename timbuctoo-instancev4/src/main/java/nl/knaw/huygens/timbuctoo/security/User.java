package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
// FIXME Find a better way for deserialization
@JsonTypeIdResolver(UserTypeIdResolver.class) // be able to map the login java type and the serialized version
@JsonIgnoreProperties(ignoreUnknown = true) // ignore the unknown properties
public class User {
  private String displayName;
  private String persistentId;
  @JsonProperty("_id")
  private String id;

  public User() {
    this.id = UUID.randomUUID().toString();
  }

  public User(String displayName) {
    this.displayName = displayName;
    this.id = UUID.randomUUID().toString();
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getPersistentId() {
    return persistentId;
  }

  public void setPersistentId(String persistentId) {
    this.persistentId = persistentId;
  }

  public String getId() {
    return id;
  }

}
