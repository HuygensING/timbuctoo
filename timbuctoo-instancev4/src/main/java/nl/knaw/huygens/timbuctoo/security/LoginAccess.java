package nl.knaw.huygens.timbuctoo.security;

import java.util.Optional;

public interface LoginAccess {

  Optional<Login> getLogin(String username) throws LocalLoginUnavailableException;

  void addLogin(Login login) throws LoginCreationException;
}
