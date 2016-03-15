package nl.knaw.huygens.timbuctoo.security;

/*
 * #%L
 * Timbuctoo REST api
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

import nl.knaw.huygens.security.client.model.SecurityInformation;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

public class SecurityInformationMatcher extends TypeSafeMatcher<SecurityInformation> {

  private final String userPid;
  private final String givenName;
  private final String surname;
  private final String emailAddress;
  private final String organization;
  private final String commonName;

  private SecurityInformationMatcher(String userPid, String givenName, String surname, String emailAddress, String organization, String commonName) {
    this.userPid = userPid;
    this.givenName = givenName;
    this.surname = surname;
    this.emailAddress = emailAddress;
    this.organization = organization;
    this.commonName = commonName;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("SecurityInformation with ");
    appendProperty(description, "persistentId", userPid);
    appendProperty(description, "givenName", givenName);
    appendProperty(description, "surname", surname);
    appendProperty(description, "commonName", commonName);
    appendProperty(description, "displayName", commonName);
    appendProperty(description, "emailAddress", emailAddress);
    appendProperty(description, "organization", organization);
  }

  @Override
  protected void describeMismatchSafely(SecurityInformation item, Description mismatchDescription) {
    mismatchDescription.appendText("SecurityInformation with ");
    appendProperty(mismatchDescription, "persistentId", item.getPersistentID());
    appendProperty(mismatchDescription, "givenName", item.getGivenName());
    appendProperty(mismatchDescription, "surname", item.getSurname());
    appendProperty(mismatchDescription, "commonName", item.getCommonName());
    appendProperty(mismatchDescription, "displayName", item.getDisplayName());
    appendProperty(mismatchDescription, "emailAddress", item.getEmailAddress());
    appendProperty(mismatchDescription, "organization", item.getOrganization());
  }

  private void appendProperty(Description description, String propertyName, String value) {
    description.appendText(" ")//
        .appendText(propertyName)//
        .appendText(": ")//
        .appendValue(value);
  }

  @Override
  protected boolean matchesSafely(SecurityInformation item) {
    if (item == null) {
      return false;
    }

    boolean matches = Objects.equal(userPid, item.getPersistentID());
    matches &= Objects.equal(givenName, item.getGivenName());
    matches &= Objects.equal(surname, item.getSurname());
    matches &= Objects.equal(emailAddress, item.getEmailAddress());
    matches &= Objects.equal(organization, item.getOrganization());
    matches &= Objects.equal(commonName, item.getCommonName());

    return matches;
  }

  public static SecurityInformationMatcher securityInformationWith(String userPid, String givenName, String surname, String emailAddress, String organization, String commonName) {
    return new SecurityInformationMatcher(userPid, givenName, surname, emailAddress, organization, commonName);
  }
}
