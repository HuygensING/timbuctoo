package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.crud.Authorization;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;

public interface Authorizer {

  Authorization authorizationFor(Collection collection, String userId) throws AuthorizationUnavailableException;

  Authorization authorizationFor(String vreId, String userId) throws AuthorizationUnavailableException;
}
