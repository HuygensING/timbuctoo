package nl.knaw.huygens.timbuctoo.security;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

public class LocalLoggedInUsers {
  static final String LOCAL_SESSION_KEY_PREFIX = "Timbuctoo";
  private final Map<String, String> sessionKeyPidMap;

  public LocalLoggedInUsers() {
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

}
