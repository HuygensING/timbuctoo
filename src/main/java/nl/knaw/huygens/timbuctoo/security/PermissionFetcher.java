package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.PermissionFetchingException;

import java.util.Set;

public interface PermissionFetcher {
  Set<Permission> getPermissions(User user, DataSetMetaData dataSetMetadata) throws PermissionFetchingException;

  boolean hasPermission(User user, DataSetMetaData dataSet, Permission permission) throws PermissionFetchingException;

  Set<Permission> getOldPermissions(User user, String vreId)
    throws PermissionFetchingException;

  void initializeOwnerAuthorization(User user, String ownerId, String dataSetId)
    throws PermissionFetchingException, AuthorizationCreationException;

  void removeAuthorizations(String combinedId) throws PermissionFetchingException;
}
