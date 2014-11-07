package nl.knaw.huygens.timbuctoo.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.model.Login;

import org.junit.Before;
import org.junit.Test;

public class BasicAuthenticationHandlerTest {
  private static final String NORMALIZED_AUTH_STRING = "authString";
  private static final String AUTH_STRING = "Basic authString";
  private LocalAuthenticator localAuthenticatorMock;
  private LocalLoggedInUsers localLoggedInUsersMock;
  private BasicAuthenticationHandler instance;

  @Before
  public void setUp() {
    localAuthenticatorMock = mock(LocalAuthenticator.class);
    localLoggedInUsersMock = mock(LocalLoggedInUsers.class);
    instance = new BasicAuthenticationHandler(localAuthenticatorMock, localLoggedInUsersMock);
  }

  @Test
  public void authenticateReturnsAnAuthenticationTokenAndRegistersTheUserAsLoggedInIfTheAuthenticationStringIsOk() throws Exception {
    // setup
    String authToken = "authToken";
    Login login = new Login();

    // when
    when(localAuthenticatorMock.authenticate(NORMALIZED_AUTH_STRING)).thenReturn(login);
    when(localLoggedInUsersMock.add(login)).thenReturn(authToken);

    // action 
    String actualAuthToken = instance.authenticate(AUTH_STRING);

    // verify
    assertThat(actualAuthToken, is(equalTo(authToken)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void authenticateThrowsAnIllegalArgumentExceptionWhenTheAuthenticationDoesNotStartWithBasic() throws Exception {
    // setup
    String authString = "test";

    try {
      // action
      instance.authenticate(authString);
    } finally {
      // verify
      verifyZeroInteractions(localAuthenticatorMock, localLoggedInUsersMock);
    }

  }

  @Test(expected = UnauthorizedException.class)
  public void authenticateThrowAnUnAuthorizedExceptionWhenTheUserNameAndPasswordAreIncorrect() throws Exception {
    // when
    doThrow(UnauthorizedException.class).when(localAuthenticatorMock).authenticate(NORMALIZED_AUTH_STRING);

    try {
      // action
      instance.authenticate(AUTH_STRING);
    } finally {
      // verify
      verifyZeroInteractions(localLoggedInUsersMock);
    }
  }
}
