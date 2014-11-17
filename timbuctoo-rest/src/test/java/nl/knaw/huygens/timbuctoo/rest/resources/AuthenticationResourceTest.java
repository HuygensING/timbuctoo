package nl.knaw.huygens.timbuctoo.rest.resources;

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
    return resource().path("authenticate");
  }

}
