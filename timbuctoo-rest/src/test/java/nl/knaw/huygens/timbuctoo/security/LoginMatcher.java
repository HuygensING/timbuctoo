package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.model.Login;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class LoginMatcher extends TypeSafeMatcher<Login> {

  private final String userName;

  @Override
  public void describeTo(Description description) {
    description.appendText("Login with userName: ")//
        .appendValue(userName);
  }

  @Override
  protected void describeMismatchSafely(Login item, Description mismatchDescription) {
    mismatchDescription.appendText("Login with userName: ")//
        .appendValue(item.getPassword());
  }

  @Override
  protected boolean matchesSafely(Login item) {
    return StringUtils.equals(userName, item.getUserName());
  }

  private LoginMatcher(String userName) {
    this.userName = userName;

  }

  public static LoginMatcher loginWithUserName(String userName) {
    return new LoginMatcher(userName);

  }

}
