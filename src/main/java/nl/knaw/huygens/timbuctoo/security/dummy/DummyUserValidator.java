package nl.knaw.huygens.timbuctoo.security.dummy;

import nl.knaw.huygens.timbuctoo.security.UserValidator;
import nl.knaw.huygens.timbuctoo.security.dto.User;

import java.util.Optional;

class DummyUserValidator implements UserValidator {
  private static final String ID = "33707283d426f900d4d33707283d426f900d4d0d";
  private static final User DUMMY = User.create("{{Mr. Test User}}", ID);

  @Override
  public Optional<User> getUserFromAccessToken(String accessToken) {
    return accessToken != null && !accessToken.isBlank() ? Optional.of(DUMMY) : Optional.empty();
  }

  @Override
  public Optional<User> getUserFromPersistentId(String persistentId) {
    return ID.equals(persistentId) ? Optional.of(DUMMY) : Optional.empty();
  }
}
