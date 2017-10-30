package nl.knaw.huygens.timbuctoo.v5.security;

import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;

import java.util.Optional;

public interface UserValidator {
  Optional<User> getUserFromAccessToken(String accessToken) throws UserValidationException;

  Optional<User> getUserFromId(String userId) throws UserValidationException;

  Optional<User> getUserFromPersistentId(String userId) throws UserValidationException;
}
