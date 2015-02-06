package nl.knaw.huygens.timbuctoo.rest.resources;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.HttpHeaders;

import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.security.BasicAuthenticationHandler;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

public class AuthenticationResourceTest extends WebServiceTestSetup {

  private static final String AUTH_STRING = "authString";
  private BasicAuthenticationHandler basicAuthHandlerMock;

  @Before
  public void setUpAuthenticationHandler() {
    basicAuthHandlerMock = injector.getInstance(BasicAuthenticationHandler.class);
  }

  @Test
  public void whenTheCorrectUserInformationIsSendASecurityTokenIsReturned() throws UnauthorizedException {
    String token = "localToken";
    when(basicAuthHandlerMock.authenticate(AUTH_STRING)).thenReturn(token);

    // action
    ClientResponse response = doAuthenticationRequest();

    // verify
    verifyResponseStatus(response, Status.NO_CONTENT);
    assertThat(response.getHeaders().get(CustomHeaders.TOKEN_HEADER).get(0), is(equalTo(token)));
  }

  @Test
  public void whenTheWrongUserInformationIsSendTheStatusUnauthorizedIsReturned() throws UnauthorizedException {
    doThrow(UnauthorizedException.class).when(basicAuthHandlerMock).authenticate(anyString());

    // action
    ClientResponse response = doAuthenticationRequest();

    // verify
    verifyResponseStatus(response, Status.UNAUTHORIZED);
  }

  @Test
  public void whenTheAuthenticationHandlerThrowsAnIllegalArgumentExceptionABadRequestStatusIsReturned() throws UnauthorizedException {
    // setup
    doThrow(IllegalArgumentException.class).when(basicAuthHandlerMock).authenticate(anyString());

    // action
    ClientResponse response = doAuthenticationRequest();

    // verify
    verifyResponseStatus(response, Status.BAD_REQUEST);
  }

  private ClientResponse doAuthenticationRequest() {
    return authenticationResource()//
        .header(HttpHeaders.AUTHORIZATION, AUTH_STRING)//
        .post(ClientResponse.class);
  }

  private WebResource authenticationResource() {
    return resource().path(getAPIVersion()).path("authenticate");
  }

}
