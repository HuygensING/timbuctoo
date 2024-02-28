package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.UserValidationException;

import java.util.Optional;

public interface UserValidator {
  Optional<User> getUserFromAccessToken(String accessToken) throws UserValidationException;

  Optional<User> getUserFromPersistentId(String persistentId) throws UserValidationException;
}
