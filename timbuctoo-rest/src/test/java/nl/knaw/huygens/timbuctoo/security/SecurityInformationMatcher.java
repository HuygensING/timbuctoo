package nl.knaw.huygens.timbuctoo.security;

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
