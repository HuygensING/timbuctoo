package nl.knaw.huygens.timbuctoo.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import nl.knaw.huygens.timbuctoo.util.Timeout;

import java.util.Optional;
import java.util.UUID;

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
    return Optional.ofNullable(authHeader)
                   .flatMap(hdr -> Optional.ofNullable(users.getIfPresent(hdr)));
  }

  public Optional<String> userTokenFor(String username, String password)
    throws LocalLoginUnavailableException, AuthenticationUnavailableException {
    Optional<String> id;
    Optional<String> token = Optional.empty();

    id = jsonBasedAuthenticator.authenticate(username, password);

    if (id.isPresent()) {
      Optional<User> user = userStore.userFor(id.get());
      if (user.isPresent()) {
        String uuid = UUID.randomUUID().toString();
        token = Optional.of(uuid);
        users.put(uuid, user.get());
      }
    }

    return token;
  }
}
