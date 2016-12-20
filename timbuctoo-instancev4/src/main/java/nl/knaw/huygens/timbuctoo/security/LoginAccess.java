package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.dto.Login;

import java.util.Optional;

public interface LoginAccess {

  Optional<Login> getLogin(String username) throws LocalLoginUnavailableException;

  void addLogin(Login login) throws LoginCreationException;
}
