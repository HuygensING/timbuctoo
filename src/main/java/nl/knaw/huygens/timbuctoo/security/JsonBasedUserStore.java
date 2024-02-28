package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.dataaccess.UserAccess;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthenticationUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class JsonBasedUserStore implements UserStore {
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
  public Optional<User> userForApiKey(String apiKey) throws AuthenticationUnavailableException {
    return userAccess.getUserForApiKey(apiKey);
  }

  @Override
  public User saveNew(String displayName, String persistentId, Map<String, String> properties) throws AuthenticationUnavailableException {
    User nw = User.create(displayName, persistentId, properties);
    userAccess.addUser(nw);
    return nw;
  }
}
