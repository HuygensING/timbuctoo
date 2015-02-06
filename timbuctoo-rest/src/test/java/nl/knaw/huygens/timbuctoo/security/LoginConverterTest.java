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

import static nl.knaw.huygens.timbuctoo.security.SecurityInformationMatcher.securityInformationWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.model.Login;

import org.junit.Test;

public class LoginConverterTest {
  @Test
  public void toSecurityInformationCreatesAPrincipalAndAddsItToANewSecurityInformation() {
    // setup
    String userPid = "userPid";
    String userName = "userName";
    String password = "password";
    String givenName = "givenName";
    String surname = "surname";
    String emailAddress = "test@test.com";
    String organization = "organization";
    String commonName = "givenName surname";
    byte[] salt = "salt".getBytes();

    Login login = new Login(userPid, userName, password, givenName, surname, emailAddress, organization, salt);

    LoginConverter instance = new LoginConverter();

    // action
    SecurityInformation information = instance.toSecurityInformation(login);

    // verify
    assertThat(information, is(securityInformationWith(userPid, givenName, surname, emailAddress, organization, commonName)));

  }
}
