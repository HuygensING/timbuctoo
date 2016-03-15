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
