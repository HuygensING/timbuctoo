package nl.knaw.huygens.timbuctoo.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.Arrays;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
// FIXME Find a better way for deserialization
@JsonTypeIdResolver(LegacyLoginTypeIdResolver.class) // be able to map the login java type and the serialized version
@JsonIgnoreProperties(ignoreUnknown = true) // ignore the unknown properties
public class LegacyLogin {

  private String userPid;
  private String password;
  private byte[] salt;
  @JsonProperty("userName")
  private String username;
  private String givenName;
  private String surName;
  private String emailAddress;
  private String organization;
  @JsonProperty("_id")
  private String id;
  @JsonProperty("^rev")
  private String rev;
  @JsonProperty("^created")
  private String created;
  @JsonProperty("^modified")
  private String modified;

  public LegacyLogin() {

  }

  public LegacyLogin(String userPid, String username, String password, byte[] salt) {
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

  public String getGivenName() {
    return givenName;
  }


  public String getSurName() {
    return surName;
  }

  public void setSurName(String surName) {
    this.surName = surName;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRev() {
    return rev;
  }

  public void setRev(String rev) {
    this.rev = rev;
  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public String getModified() {
    return modified;
  }


  public void setModified(String modified) {
    this.modified = modified;
  }
}
