package nl.knaw.huygens.timbuctoo.security;

import static nl.knaw.huygens.timbuctoo.security.LocalAuthenticator.LOGIN_COLLECTION_FILE_NAME;

import java.util.Map;
import java.util.UUID;

import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.file.JsonFileHandler;
import nl.knaw.huygens.timbuctoo.storage.file.LoginCollection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class LocalLoggedInUsers implements AuthenticationHandler {
  private static final Logger LOG = LoggerFactory.getLogger(LocalLoggedInUsers.class);
  static final String LOCAL_SESSION_KEY_PREFIX = "Timbuctoo";
  private final Map<String, String> sessionKeyPidMap;
  private final JsonFileHandler jsonFileHandler;
  private final LoginConverter loginConverter;

  @Inject
  public LocalLoggedInUsers(JsonFileHandler jsonFileHandler, LoginConverter loginConverter) {
    this.jsonFileHandler = jsonFileHandler;
    this.loginConverter = loginConverter;
    sessionKeyPidMap = Maps.newConcurrentMap();
  }

  public synchronized String add(String persistentId) {
    String sessionKey = createSessionKey();

    sessionKeyPidMap.put(sessionKey, persistentId);

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
  public synchronized String get(String sessionKey) {
    return sessionKeyPidMap.get(sessionKey);
  }

  @Override
  public SecurityInformation getSecurityInformation(String sessionKey) throws UnauthorizedException {
    String pid = get(sessionKey);

    if (StringUtils.isBlank(pid)) {
      throw new UnauthorizedException("Session could not be retrieved.");
    }

    try {
      LoginCollection loginCollection = jsonFileHandler.getCollection(LoginCollection.class, LOGIN_COLLECTION_FILE_NAME);

      Login example = new Login();
      example.setUserPid(pid);

      Login login = loginCollection.findItem(example);

      if (login == null) {
        throw new UnauthorizedException("Session could not be retrieved.");
      }

      return loginConverter.toSecurityInformation(login);

    } catch (StorageException e) {
      LOG.error("Login collection could not be loaded: {}", e);
      throw new UnauthorizedException("Session could not be retrieved.");
    }
  }
}
