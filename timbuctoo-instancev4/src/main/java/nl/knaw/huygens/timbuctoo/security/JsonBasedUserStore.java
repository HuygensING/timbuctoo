package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class JsonBasedUserStore implements UserStore, UserCreator {

  public static final Logger LOG = LoggerFactory.getLogger(JsonBasedUserStore.class);
  private final UserAccess userAccess;

  public JsonBasedUserStore(UserAccess userAccess) {
    this.userAccess = userAccess;
  }

  @Override
  public Optional<User> userFor(String pid) throws AuthenticationUnavailableException {
    return userAccess.getUserForPid(pid);
  }

  @Override
  public Optional<User> userForId(String userId) throws AuthenticationUnavailableException {
    return userAccess.getUserForTimLocalId(userId);
  }

  @Override
  public User saveNew(String displayName, String persistentId) throws AuthenticationUnavailableException {
    User nw = new User(displayName);
    nw.setPersistentId(persistentId);
    userAccess.addUser(nw);
    return nw;
  }

  @Override
  public String createUser(String pid, String email, String givenName, String surname, String organization)
    throws UserCreationException {
    User user = new User();
    user.setPersistentId(pid);
    user.setDisplayName(String.format("%s %s", givenName, surname));
    try {
      Optional<User> userForPid = userAccess.getUserForPid(pid);
      if (userForPid.isPresent()) {
        return userForPid.get().getId();
      } else {
        userAccess.addUser(user);
        return user.getId();
      }
    } catch (AuthenticationUnavailableException e) {
      throw new UserCreationException(e);
    }
  }
}
