package nl.knaw.huygens.timbuctoo.security;

import java.util.Map;
import java.util.UUID;

import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.model.Login;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class LocalLoggedInUsers implements AuthenticationHandler {
  static final String LOCAL_SESSION_KEY_PREFIX = "Timbuctoo";
  private final Map<String, Login> sessionKeyPidMap;
  private final LoginConverter loginConverter;

  @Inject
  public LocalLoggedInUsers(LoginConverter loginConverter) {
    this.loginConverter = loginConverter;
    sessionKeyPidMap = Maps.newConcurrentMap();
  }

  public synchronized String add(Login login) {
    String sessionKey = createSessionKey();

    sessionKeyPidMap.put(sessionKey, login);

    return sessionKey;
  }

  private String createSessionKey() {

    return String.format("%s%s", LOCAL_SESSION_KEY_PREFIX, UUID.randomUUID());
  }

  /**
   * Retrieves the persistent id.
   * @param sessionKey the key to retrieve the id for.
   * @return the id or {@code null} if none found.
   */
  public synchronized Login get(String sessionKey) {
    return sessionKeyPidMap.get(sessionKey);
  }

  @Override
  public SecurityInformation getSecurityInformation(String sessionKey) throws UnauthorizedException {
    Login login = get(sessionKey);

    if (login == null) {
      throw new UnauthorizedException("Session could not be retrieved.");
    }

    return loginConverter.toSecurityInformation(login);
  }
}
