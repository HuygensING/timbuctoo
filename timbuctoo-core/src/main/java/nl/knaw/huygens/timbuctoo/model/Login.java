package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

import com.fasterxml.jackson.annotation.JsonIgnore;

@IDPrefix(Login.ID_PREFIX)
public class Login extends SystemEntity {

  public static final String ID_PREFIX = "LOGI";
  private String userPid;
  private String password;
  private String givenName;
  private String surname;
  private String emailAddress;
  private String organization;
  private byte[] salt;
  private String userName;

  public Login() {

  }

  public Login(String userPid, String userName, String password, String givenName, String surname, String emailAddress, String organization, byte[] salt) {
    this.userPid = userPid;
    this.userName = userName;
    this.password = password;
    this.givenName = givenName;
    this.surname = surname;
    this.emailAddress = emailAddress;
    this.organization = organization;
    this.salt = salt;
  }

  @Override
  public String getIdentificationName() {
    return getCommonName();
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

  public byte[] getSalt() {
    return salt;
  }

  public void setSalt(byte[] salt) {
    this.salt = salt;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

}
