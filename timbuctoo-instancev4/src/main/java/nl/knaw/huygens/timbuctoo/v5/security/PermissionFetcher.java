package nl.knaw.huygens.timbuctoo.v5.security;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;

import java.util.Set;

public interface PermissionFetcher {
  Set<Permission> getPermissions(String persistentId, PromotedDataSet dataSetMetadata)
    throws PermissionFetchingException;

  Set<Permission> getOldPermissions(String persistentId, String vreId)
    throws PermissionFetchingException;

  void initializeOwnerAuthorization(String userId, String ownerId, String dataSetId)
    throws PermissionFetchingException, AuthorizationCreationException;

  void removeAuthorizations(String ownerId, String vreId) throws PermissionFetchingException;
}
