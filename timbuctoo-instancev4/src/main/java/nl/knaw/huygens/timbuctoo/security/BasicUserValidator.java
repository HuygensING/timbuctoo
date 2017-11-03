package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.security.client.AuthenticationHandler;
import nl.knaw.huygens.security.client.UnauthorizedException;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;

import java.io.IOException;
import java.util.Optional;

class BasicUserValidator implements UserValidator {

  private final AuthenticationHandler authenticationHandler;
  private final UserStore userStore;

  BasicUserValidator(AuthenticationHandler authenticationHandler, UserStore userStore) {

    this.authenticationHandler = authenticationHandler;
    this.userStore = userStore;
  }

  @Override
  public Optional<User> getUserFromAccessToken(String accessToken) throws UserValidationException {
    if (accessToken != null) {
      try {
        SecurityInformation securityInformation = authenticationHandler.getSecurityInformation(accessToken);

        if (securityInformation != null) {
          Optional<User> user = userStore.userFor(securityInformation.getPersistentID());
          if (!user.isPresent()) {
            User newUser = userStore.saveNew(securityInformation.getDisplayName(),
              securityInformation.getPersistentID());

            return Optional.of(newUser);
          }
          return user;
        }
      } catch (UnauthorizedException | IOException | AuthenticationUnavailableException e) {
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
