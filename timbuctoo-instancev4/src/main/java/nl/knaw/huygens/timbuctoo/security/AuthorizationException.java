package nl.knaw.huygens.timbuctoo.security;

import java.util.UUID;

public class AuthorizationException extends Exception {
  public AuthorizationException(String message) {
    super(message);
  }

  public static AuthorizationException notAllowedToDelete(String collectionName, UUID id) {
    return new AuthorizationException(
      String.format("You are not allowed to delete item with \"%s\" from \"%s\".", collectionName, id));
  }
}
