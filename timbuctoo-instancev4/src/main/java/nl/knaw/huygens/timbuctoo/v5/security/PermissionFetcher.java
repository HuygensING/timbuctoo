package nl.knaw.huygens.timbuctoo.v5.security;

import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;

import java.util.Set;

public interface PermissionFetcher {
  Set<Permission> getPermissions(String persistentId, String ownerId, String dataSetId)
    throws PermissionFetchingException;

  Set<Permission> getPermissions(String persistentId, String vreId)
    throws PermissionFetchingException;

  void initializeOwnerAuthorization(String userId, String ownerId, String dataSetId)
    throws PermissionFetchingException, AuthorizationCreationException;

  void removeAuthorizations(String ownerId, String dataSetId) throws PermissionFetchingException;
}
