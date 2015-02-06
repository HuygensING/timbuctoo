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

import static nl.knaw.huygens.timbuctoo.security.LocalLoggedInUsers.LOCAL_SESSION_KEY_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.EnumSet;

import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.security.core.model.Affiliation;
import nl.knaw.huygens.timbuctoo.model.Login;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.cache.Cache;

public class LocalLoggedInUsersTest {
  private LocalLoggedInUsers instance;
  private LoginConverter loginConverterMock;
  @Mock
  private Cache<String, Login> cacheMock;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    loginConverterMock = mock(LoginConverter.class);
    instance = new LocalLoggedInUsers(loginConverterMock, cacheMock);
  }

  @Test
  public void addMakesThePidOfTheUserRetrievableAndReturnsTheKeyToRetrieveIt() {
    Login login = new Login();

    // action
    String sessionKey = instance.add(login);

    // verify
    verify(cacheMock).put(sessionKey, login);
  }

  @Test
  public void addReturnsAKeyThatStartsWithTheLocalSessionIdPrefix() {
    // action
    String sessionKey = instance.add(new Login());

    // verify
    assertThat(sessionKey, startsWith(LOCAL_SESSION_KEY_PREFIX));
  }

  @Test
  public void getSecurityInformationReturnsTheSecurityInformationWithTheUserPID() throws Exception {
    // setup
    Login login = new Login();
    String sessionKey = "anyString";

    // when
    when(cacheMock.getIfPresent(anyString())).thenReturn(login);

    SecurityInformation securityInformation = createSecurityInformation();

    // when
    when(loginConverterMock.toSecurityInformation(login)).thenReturn(securityInformation);

    // action
    SecurityInformation actualSecurityInformation = instance.getSecurityInformation(sessionKey);

    // verify
    assertThat(actualSecurityInformation, is(securityInformation));

  }

  private SecurityInformation createSecurityInformation() {
    return new SecurityInformation() {

      @Override
      public String getSurname() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Principal getPrincipal() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getPersistentID() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getOrganization() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getGivenName() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getEmailAddress() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getDisplayName() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getCommonName() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public EnumSet<Affiliation> getAffiliations() {
        // TODO Auto-generated method stub
        return null;
      }
    };
  }

  @Test(expected = UnauthorizedException.class)
  public void getSecurityInformationItThrowsAnUnauthorizedExceptionWhenTheSessionCannotBeFound() throws Exception {
    // setup
    String unknownSessionKey = "unknownSessionKey";

    try {
      // action
      instance.getSecurityInformation(unknownSessionKey);
    } finally {
      verify(cacheMock).getIfPresent(unknownSessionKey);
    }
  }

}
