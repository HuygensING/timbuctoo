package nl.knaw.huygens.timbuctoo.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.LocalLoginUnavailableException;
import nl.knaw.huygens.timbuctoo.util.Timeout;
import nl.knaw.huygens.timbuctoo.v5.security.BasicUserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

/**
 * Will determine if a user's credentials are valid and return a token.
 * Will be able to return the user belonging to a token.
 * <p/>
 * <p>Needs: An object from which to retrieve valid users, an object to store logged in users</p>
 */
public class LoggedInUsers {

  public static final Logger LOG = LoggerFactory.getLogger(LoggedInUsers.class);
  private final Authenticator authenticator;
  private final Cache<String, User> users;
  private final UserValidator userValidator;

  public LoggedInUsers(Authenticator authenticator, UserValidator userValidator,
                       Timeout inactivityTimeout) {
    this.authenticator = authenticator;
    this.users = createCache(inactivityTimeout);
    this.userValidator = userValidator;
  }

  private static Cache<String, User> createCache(Timeout timeout) {
    return CacheBuilder.newBuilder().expireAfterAccess(timeout.duration, timeout.timeUnit).build();
  }

  public Optional<User> userFor(String authHeader) {
    if (authHeader == null || authHeader.isEmpty()) {
      return Optional.empty();
    } else {
      User local = users.getIfPresent(authHeader);
      if (local != null) {
        return Optional.of(local);
      } else {
        Optional<User> user = null;
        try {
          user = userValidator.getUserFromAccessToken(authHeader);
          if (user.isPresent()) {
            users.put(authHeader, user.get());
          }
        } catch (UserValidationException e) {
          return Optional.empty();
        }
        return user;
      }
    }
  }

  public Optional<String> userTokenFor(String username, String password)
    throws LocalLoginUnavailableException, UserValidationException {
    Optional<String> id;
    Optional<String> token = Optional.empty();

    id = authenticator.authenticate(username, password);

    if (id.isPresent()) {
      Optional<User> user;
      try {
        user = userValidator.getUserFromPersistentId(id.get());
      } catch (UserValidationException e) {
        throw new UserValidationException(e);
      }
      if (user.isPresent()) {
        String uuid = UUID.randomUUID().toString();
        token = Optional.of(uuid);
        users.put(uuid, user.get());
      }
    }
    return token;
  }
}
