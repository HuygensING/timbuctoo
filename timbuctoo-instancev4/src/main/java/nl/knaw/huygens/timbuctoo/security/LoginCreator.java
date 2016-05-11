package nl.knaw.huygens.timbuctoo.security;

public interface LoginCreator {
  void createLogin(String userPid, String userName, String password, String givenName, String surname, String email,
                   String organization) throws LoginCreationException;
}
