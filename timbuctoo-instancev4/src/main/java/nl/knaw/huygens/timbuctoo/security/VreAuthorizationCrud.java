package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;

public interface VreAuthorizationCrud {
  void createAuthorization(String vreId, String userId, String vreRole) throws AuthorizationCreationException;

  /**
   * Removes all the authorizations from the Vre.
   * @param vreId the id of the Vre of which all the authorizations should be removed from.
   * @param user  the id of the user that executes the method
   * @throws AuthorizationException if the user with userId does not have the permissions needed
   * @throws AuthorizationUnavailableException when the authorizations could not be read
   */
  void deleteVreAuthorizations(String vreId, User user) throws AuthorizationException,
    AuthorizationUnavailableException;
}
