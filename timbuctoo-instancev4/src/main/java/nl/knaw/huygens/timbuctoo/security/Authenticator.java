package nl.knaw.huygens.timbuctoo.security;

import java.util.Optional;

public interface Authenticator {
  Optional<String> authenticate(String username, String password) throws LocalLoginUnavailableException;
}
