package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.model.Login;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class LoginMatcher extends TypeSafeMatcher<Login> {

  private final String authString;

  @Override
  public void describeTo(Description description) {
    description.appendText("Login with authString: ")//
        .appendValue(authString);
  }

  @Override
  protected void describeMismatchSafely(Login item, Description mismatchDescription) {
    mismatchDescription.appendText("Login with authString: ")//
        .appendValue(item.getAuthString());
  }

  @Override
  protected boolean matchesSafely(Login item) {
    return StringUtils.equals(authString, item.getAuthString());
  }

  private LoginMatcher(String authString) {
    this.authString = authString;

  }

  public static LoginMatcher loginWithAuthString(String authString) {
    return new LoginMatcher(authString);

  }

}
