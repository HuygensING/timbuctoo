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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.EnumSet;
import java.util.UUID;

import nl.knaw.huygens.security.client.HuygensAuthenticationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.security.core.model.Affiliation;

import org.junit.Before;
import org.junit.Test;

public class TimbuctooAuthenticationHandlerTest {

  private LocalLoggedInUsers localLoggedInUsersMock;
  private TimbuctooAuthenticationHandler instance;
  private SecurityInformation securityInformation;
  HuygensAuthenticationHandler huygensAuthenticationHandlerMock;

  @Before
  public void setUp() {
    localLoggedInUsersMock = mock(LocalLoggedInUsers.class);
    huygensAuthenticationHandlerMock = mock(HuygensAuthenticationHandler.class);
    instance = new TimbuctooAuthenticationHandler(localLoggedInUsersMock, huygensAuthenticationHandlerMock);
    securityInformation = createSecurityInformation();
  }

  @Test
  public void getSecurityInformationRequestsDelegatesTheRequestToLocalLoggedInUsersWhenTheSessionIdIsLocal() throws UnauthorizedException {
    //setUp
    String localSessionId = createLocalSessionId();

    // when
    when(localLoggedInUsersMock.getSecurityInformation(localSessionId)).thenReturn(securityInformation);

    // action
    SecurityInformation actualSecurityInformation = instance.getSecurityInformation(localSessionId);

    // verify
    assertThat(actualSecurityInformation, is(securityInformation));
    verify(localLoggedInUsersMock).getSecurityInformation(localSessionId);
    verifyZeroInteractions(huygensAuthenticationHandlerMock);
  }

  @Test
  public void getSecurityInformationDelegatesTheRequestToHuygensAuthenticationHandler() throws UnauthorizedException {
    // setup
    String sessionId = createSessionId();

    // when
    when(huygensAuthenticationHandlerMock.getSecurityInformation(sessionId)).thenReturn(securityInformation);

    // action
    SecurityInformation actualSecurityInformation = instance.getSecurityInformation(sessionId);

    // verify
    assertThat(actualSecurityInformation, is(securityInformation));
    verify(huygensAuthenticationHandlerMock).getSecurityInformation(sessionId);
    verifyZeroInteractions(localLoggedInUsersMock);
  }

  @Test(expected = UnauthorizedException.class)
  public void getSecurityInformationRethrowsTheUnauthorizedExceptionOfLocalLoggedInUsers() throws UnauthorizedException {
    // setup
    String localSessionId = createLocalSessionId();

    // when
    doThrow(UnauthorizedException.class).when(localLoggedInUsersMock).getSecurityInformation(localSessionId);

    // action
    instance.getSecurityInformation(localSessionId);
  }

  @Test(expected = UnauthorizedException.class)
  public void getSecurityInformationRethrowsTheUnauthorizedExceptionOfHuygensAuthenticationHandler() throws UnauthorizedException {
    // setup
    String sessionId = createSessionId();

    // when
    doThrow(UnauthorizedException.class).when(huygensAuthenticationHandlerMock).getSecurityInformation(sessionId);

    // action
    instance.getSecurityInformation(sessionId);
  }

  private String createSessionId() {
    return UUID.randomUUID().toString();
  }

  private String createLocalSessionId() {
    return String.format("%s%s", LOCAL_SESSION_KEY_PREFIX, UUID.randomUUID());
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

}
