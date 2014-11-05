package nl.knaw.huygens.timbuctoo.security;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.model.Login;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class LocalLoggedInUsers implements AuthenticationHandler {
  private static final TimeUnit EXPIRY_TIME_UNIT = TimeUnit.HOURS;
  private static final int EXPIRY_TIME = 8;
  static final String LOCAL_SESSION_KEY_PREFIX = "Timbuctoo";
  private final LoginConverter loginConverter;
  private final Cache<String, Login> sessionCache;

  @Inject
  public LocalLoggedInUsers(LoginConverter loginConverter) {
    this(loginConverter, createCache());
  }

  private static Cache<String, Login> createCache() {
    return CacheBuilder.newBuilder().expireAfterAccess(EXPIRY_TIME, EXPIRY_TIME_UNIT).build();
  }

  LocalLoggedInUsers(LoginConverter loginConverterMock, Cache<String, Login> sessionCache) {
    loginConverter = loginConverterMock;
    this.sessionCache = sessionCache;
  }

  public synchronized String add(Login login) {
    String sessionKey = createSessionKey();

    sessionCache.put(sessionKey, login);

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
    return sessionCache.getIfPresent(sessionKey);
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
