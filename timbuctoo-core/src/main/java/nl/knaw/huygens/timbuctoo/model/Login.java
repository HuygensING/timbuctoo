package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

import com.fasterxml.jackson.annotation.JsonIgnore;

@IDPrefix(Login.ID_PREFIX)
public class Login extends SystemEntity {

  public static final String ID_PREFIX = "LOGI";
  private String userPid;
  private String authString;
  private String givenName;
  private String surname;
  private String emailAddress;
  private String organization;

  public Login() {

  }

  public Login(String userPid, String authString, String givenName, String surname, String emailAddress, String organization) {
    this.userPid = userPid;
    this.authString = authString;
    this.givenName = givenName;
    this.surname = surname;
    this.emailAddress = emailAddress;
    this.organization = organization;
  }

  @Override
  public String getDisplayName() {
    return getCommonName();
  }

  public void setUserPid(String userPid) {
    this.userPid = userPid;
  }

  public String getUserPid() {
    return userPid;
  }

  public String getAuthString() {
    return authString;
  }

  public void setAuthString(String authString) {
    this.authString = authString;
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  @JsonIgnore
  public String getCommonName() {
    return String.format("%s %s", givenName, surname);
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

}
