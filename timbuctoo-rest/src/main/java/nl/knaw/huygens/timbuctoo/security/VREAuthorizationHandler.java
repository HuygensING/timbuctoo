package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;

public interface VREAuthorizationHandler {

  /**
   * Retrieves the permissions of the user on the VRE.
   * @param user the user to get the VREAuthorization for.
   * @param vreId the id of the VRE to get the VREAuthorization for.
   * @return
   */
  public abstract VREAuthorization getVREAuthorization(User user, String vreId);

}