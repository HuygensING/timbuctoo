package nl.knaw.huygens.timbuctoo.security;

import java.util.Optional;

public interface UserAccess {
  void addUser(User user) throws AuthenticationUnavailableException;

  Optional<User> getUserForPid(String pid) throws AuthenticationUnavailableException;

  Optional<User> getUserForTimLocalId(String userId) throws AuthenticationUnavailableException;
}
