package nl.knaw.huygens.timbuctoo.server.rest;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Optional;

/**
 * Will determine if a user's credentials are valid and return a token.
 * Will be able to return the user belonging to a token.
 * <p/>
 * <p>Needs: An object from which to retrieve valid users, an object to store logged in users</p>
 */
public class LoggedInUserStore {

  private final JsonBasedAuthenticator jsonBasedAuthenticator;
  private final JsonBasedUserStore userStore;
  private final Cache<String, User> users;

  public LoggedInUserStore(JsonBasedAuthenticator jsonBasedAuthenticator, JsonBasedUserStore userStore,
                           Timeout inactivityTimeout) {
    this.jsonBasedAuthenticator = jsonBasedAuthenticator;
    this.userStore = userStore;
    this.users = createCache(inactivityTimeout);
  }

  private static Cache<String, User> createCache(Timeout timeout) {
    return CacheBuilder.newBuilder().expireAfterAccess(timeout.duration, timeout.timeUnit).build();
  }

  public Optional<User> userFor(String authHeader) {
    return Optional.ofNullable(users.getIfPresent(authHeader));
  }

  public Optional<String> userTokenFor(String username, String password) throws LocalLoginUnavailableException {
    Optional<String> id;
    try {
      id = jsonBasedAuthenticator.authenticate(username, password);
    } catch (LocalLoginUnavailableException e) {
      throw e;
    }
    if (id.isPresent()) {
      try {
        Optional<User> user = userStore.userFor(id.get());
        if (user.isPresent()) {
          users.put(id.get(), user.get());
        }
      } catch (AuthenticationUnavailableException e) {
        e.printStackTrace();
      }
    }

    return id;
  }
}
