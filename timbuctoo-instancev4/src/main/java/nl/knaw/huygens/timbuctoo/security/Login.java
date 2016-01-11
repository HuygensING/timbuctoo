package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
// FIXME Find a better way for deserialization
@JsonTypeIdResolver(LoginTypeIdResolver.class) // be able to map the login java type and the serialized version
@JsonIgnoreProperties(ignoreUnknown = true) // ignore the unknown properties
public class Login {

  private String userPid;
  private String password;
  private byte[] salt;
  @JsonProperty("userName")
  private String username;

  public Login() {

  }

  public Login(String userPid, String username, String password, byte[] salt) {
    this.userPid = userPid;
    this.username = username;
    this.password = password;
    this.salt = salt;
  }

  public void setUserPid(String userPid) {
    this.userPid = userPid;
  }

  public String getUserPid() {
    return userPid;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String authString) {
    this.password = authString;
  }

  public byte[] getSalt() {
    return salt;
  }

  public void setSalt(byte[] salt) {
    this.salt = salt;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
