package nl.knaw.huygens.timbuctoo.security;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LocalUserCreatorTest {

  private static final String PID = "pid";
  private static final String USER_NAME = "user_name";
  private static final String PWD = "pwd";
  private static final String GIVEN_NAME = "Given";
  private static final String SURNAME = "Sur";
  private static final String EMAIL = "test@example.com";
  private static final String ORGANIZATION = "Org";
  private static final String USER_ID_CREATED_BY_USER_CREATOR = "userId";
  private static final String VRE_ID = "VRE";
  private static final String VRE_ROLE = "role";
  private LoginCreator loginCreator;
  private UserCreator userCreator;
  private VreAuthorizationCreator authorizationCreator;
  private LocalUserCreator instance;
  private Map<String, String> userInfo;

  @Before
  public void setup() throws Exception {
    loginCreator = mock(LoginCreator.class);
    userCreator = mock(UserCreator.class);
    given(userCreator.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
      .willReturn(USER_ID_CREATED_BY_USER_CREATOR);
    authorizationCreator = mock(VreAuthorizationCreator.class);
    instance = new LocalUserCreator(loginCreator, userCreator, authorizationCreator);

    userInfo = Maps.newHashMap();
    userInfo.put("userPid", PID);
    userInfo.put("userName", USER_NAME);
    userInfo.put("password", PWD);
    userInfo.put("givenName", GIVEN_NAME);
    userInfo.put("surname", SURNAME);
    userInfo.put("emailAddress", EMAIL);
    userInfo.put("organization", ORGANIZATION);
    userInfo.put("vreId", VRE_ID);
    userInfo.put("vreRole", VRE_ROLE);
  }

  @Test
  public void createCreatesALoginAUserAndAVreAuthorization() throws Exception {
    instance.create(userInfo);

    verify(loginCreator).createLogin(PID, USER_NAME, PWD, GIVEN_NAME, SURNAME, EMAIL, ORGANIZATION);
    verify(userCreator).createUser(PID, EMAIL, GIVEN_NAME, SURNAME, ORGANIZATION);
    verify(authorizationCreator).createAuthorization(VRE_ID, USER_ID_CREATED_BY_USER_CREATOR, VRE_ROLE);
  }


  @Test(expected = UserCreationException.class)
  public void createThrowsAUserCreationExceptionWhenTheLoginCreatorThrowsALoginCreationException() throws Exception {
    doThrow(new LoginCreationException("")).when(loginCreator)
                                           .createLogin(anyString(), anyString(), anyString(), anyString(), anyString(),
                                             anyString(),
                                             anyString());

    instance.create(userInfo);
  }

  @Test(expected = UserCreationException.class)
  public void createThrowsAUserCreationExceptionWhenTheUserCreatorDoes() throws Exception {
    doThrow(new UserCreationException("")).when(userCreator)
                                          .createUser(anyString(), anyString(), anyString(), anyString(), anyString());

    instance.create(userInfo);
  }

  @Test(expected = UserCreationException.class)
  public void createThrowsAUserCreationExceptionWhenTheAuthorizationCreatorThrowsAnAuthorizationCreationException()
    throws Exception {
    doThrow(new AuthorizationCreationException("")).when(authorizationCreator)
                                                   .createAuthorization(anyString(), anyString(), anyString());

    instance.create(userInfo);
  }
}
