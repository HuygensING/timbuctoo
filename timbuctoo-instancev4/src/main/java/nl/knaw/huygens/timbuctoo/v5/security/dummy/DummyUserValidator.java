package nl.knaw.huygens.timbuctoo.v5.security.dummy;

import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.apache.jena.sparql.algebra.Op;

import java.util.Optional;

class DummyUserValidator implements UserValidator {
  private static final String ID = "33707283d426f900d4d33707283d426f900d4d0d";
  private static final User DUMMY = User.create("{{Mr. Test User}}", ID, ID);

  @Override
  public Optional<User> getUserFromAccessToken(String accessToken) {
    return accessToken != null && !accessToken.isBlank() ? Optional.of(DUMMY) : Optional.empty();
  }

  @Override
  public Optional<User> getUserFromUserId(String userId) {
    return ID.equals(userId) ? Optional.of(DUMMY) : Optional.empty();
  }

  @Override
  public Optional<User> getUserFromPersistentId(String persistentId) {
    return ID.equals(persistentId) ? Optional.of(DUMMY) : Optional.empty();
  }
}
