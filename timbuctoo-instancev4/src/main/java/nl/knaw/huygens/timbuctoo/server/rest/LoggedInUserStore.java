package nl.knaw.huygens.timbuctoo.server.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * Will determine if a user's credentials are valid and return a token.
 * Will be able to return the user belonging to a token.
 *
 * <p>Needs: An object from which to retrieve valid users, an object to store logged in users</p>
 */
public class LoggedInUserStore {

  private final JsonBasedAuthenticator jsonBasedAuthenticator;
  private Map<String, User> users;

  public LoggedInUserStore(JsonBasedAuthenticator jsonBasedAuthenticator) {
    this.jsonBasedAuthenticator = jsonBasedAuthenticator;
    users = new HashMap<>();
  }

  public User userFor(String authHeader) {
    return users.get(authHeader);
  }

  public String userTokenFor(String username, String password) {
    String id = null;
    try {
      id = jsonBasedAuthenticator.authenticate(username, password);
    } catch (LocalLoginUnavailableException e) {
      e.printStackTrace();
    }
    users.put(id, new User());
    return id;
  }
}
