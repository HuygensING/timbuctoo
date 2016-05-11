package nl.knaw.huygens.timbuctoo.security;

import java.util.Optional;

public interface UserStore {
  Optional<User> userFor(String pid) throws AuthenticationUnavailableException;

  Optional<User> userForId(String userId) throws AuthenticationUnavailableException;

  User saveNew(String displayName, String persistentId) throws AuthenticationUnavailableException;
}
