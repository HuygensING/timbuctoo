package nl.knaw.huygens.timbuctoo.security;

public interface UserCreator {
  /**
   * @return the internal user id.
   */
  String createUser(String pid, String email, String givenName, String surname, String organization)
    throws UserCreationException;
}
