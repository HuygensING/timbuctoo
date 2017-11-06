package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;

import java.util.Optional;

class BasicUserValidator implements UserValidator {

  private final AuthenticationHandler authenticationHandler;
  private final UserStore userStore;
  private final LoggedInUsers loggedInUsers;

  BasicUserValidator(AuthenticationHandler authenticationHandler, UserStore userStore, LoggedInUsers loggedInUsers) {
    this.authenticationHandler = authenticationHandler;
    this.userStore = userStore;
    this.loggedInUsers = loggedInUsers;
  }

  @Override
  public Optional<User> getUserFromAccessToken(String accessToken) throws UserValidationException {
    if (accessToken != null) {
      try {
        Optional<User> user = loggedInUsers.userFor(accessToken);
        return user;
      } catch (Exception e) {
        throw new UserValidationException(e);
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<User> getUserFromId(String userId) throws UserValidationException {
    if (userId != null) {
      try {
        return userStore.userForId(userId);
      } catch (AuthenticationUnavailableException e) {
        throw new UserValidationException(e);
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<User> getUserFromPersistentId(String userId) throws UserValidationException {
    if (userId != null) {
      try {
        return userStore.userFor(userId);
      } catch (AuthenticationUnavailableException e) {
        throw new UserValidationException(e);
      }
    }
    return Optional.empty();
  }

}
