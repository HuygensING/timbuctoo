package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthenticationUnavailableException;

import java.util.Map;
import java.util.Optional;

public interface UserStore {
  Optional<User> userFor(String pid) throws AuthenticationUnavailableException;

  Optional<User> userForApiKey(String apiKey) throws AuthenticationUnavailableException;

  User saveNew(String displayName, String persistentId, Map<String, String> properties) throws AuthenticationUnavailableException;
}
