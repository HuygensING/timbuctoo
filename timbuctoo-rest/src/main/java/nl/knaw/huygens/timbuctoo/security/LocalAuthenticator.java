package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.LoginCollection;

public class LocalAuthenticator {

  private final LoginCollection loginCollection;

  public LocalAuthenticator(LoginCollection loginCollection) {
    this.loginCollection = loginCollection;
  }

  public String authenticate(String normalizedAuthString) throws UnauthorizedException {
    Login example = new Login();
    example.setAuthString(normalizedAuthString);

    Login login = loginCollection.findItem(example);

    if (login != null) {

      return login.getUserPid();
    }

    throw new UnauthorizedException("User name and password are unknown.");
  }
}
