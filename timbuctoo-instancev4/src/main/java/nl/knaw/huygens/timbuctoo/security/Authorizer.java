package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;

public interface Authorizer {

  Authorization authorizationFor(Collection collection, String userId) throws AuthorizationUnavailableException;

  Authorization authorizationFor(String vreId, String userId) throws AuthorizationUnavailableException;
}
