package nl.knaw.huygens.timbuctoo.security;

import java.util.Map;

public class LocalUserCreator {

  private final LoginCreator loginCreator;
  private final UserCreator userCreator;
  private final VreAuthorizationCreator authorizationCreator;

  public LocalUserCreator(LoginCreator loginCreator, UserCreator userCreator,
                          VreAuthorizationCreator authorizationCreator) {

    this.loginCreator = loginCreator;
    this.userCreator = userCreator;
    this.authorizationCreator = authorizationCreator;
  }

  public void create(Map<String, String> userInfo) throws UserCreationException {
    String userPid = userInfo.get("userPid");
    String userName = userInfo.get("userName");
    String password = userInfo.get("password");
    String givenName = userInfo.get("givenName");
    String surname = userInfo.get("surname");
    String emailAddress = userInfo.get("emailAddress");
    String organization = userInfo.get("organization");
    String vreId = userInfo.get("vreId");
    String vreRole = userInfo.get("vreRole");

    try {
      loginCreator.createLogin(userPid, userName, password, givenName, surname, emailAddress, organization);
      String userId = userCreator.createUser(userPid, emailAddress, givenName, surname, organization);
      authorizationCreator.createAuthorization(vreId, userId, vreRole);
    } catch (LoginCreationException e) {
      throw new UserCreationException(e);
    } catch (AuthorizationCreationException e) {
      throw new UserCreationException(e);
    }
  }

}
