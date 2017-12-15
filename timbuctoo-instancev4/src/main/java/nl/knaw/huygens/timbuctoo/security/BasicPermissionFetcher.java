package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData.createCombinedId;

public class BasicPermissionFetcher implements PermissionFetcher {
  private final VreAuthorizationCrud vreAuthorizationCrud;
  private final UserValidator userValidator;

  public BasicPermissionFetcher(VreAuthorizationCrud vreAuthorizationCrud, UserValidator userValidator) {
    this.vreAuthorizationCrud = vreAuthorizationCrud;
    this.userValidator = userValidator;
  }

  @Override
  public Set<Permission> getPermissions(User user, DataSetMetaData dataSetMetadata)
    throws PermissionFetchingException {
    String ownerId = dataSetMetadata.getOwnerId();
    String dataSetId = dataSetMetadata.getDataSetId();

    String vreId = createCombinedId(ownerId, dataSetId);
    Set<Permission> permissions = new HashSet<>();

    if (dataSetMetadata.isPublished()) {
      permissions.add(Permission.READ);
    }

    try {
      if (user != null) {
        Optional<VreAuthorization> vreAuthorization = vreAuthorizationCrud.getAuthorization(vreId, user);
        if (vreAuthorization.isPresent()) {
          if (vreAuthorization.get().isAllowedToWrite()) {
            permissions.add(Permission.WRITE);
            permissions.add(Permission.READ);
          }
          if (vreAuthorization.get().hasAdminAccess()) {
            permissions.add(Permission.ADMIN);
            permissions.add(Permission.READ);
          }
        }
      }
      return permissions;
    } catch (AuthorizationUnavailableException e) {
      return permissions;
    }
  }

  @Override
  @Deprecated
  public Set<Permission> getOldPermissions(User user, String vreId)
    throws PermissionFetchingException {
    Set<Permission> permissions = new HashSet<>();

    permissions.add(Permission.READ);

    try {
      Optional<VreAuthorization> vreAuthorization = vreAuthorizationCrud.getAuthorization(vreId, user);
      if (vreAuthorization.isPresent()) {
        if (vreAuthorization.get().isAllowedToWrite()) {
          permissions.add(Permission.WRITE);
        }
        if (vreAuthorization.get().hasAdminAccess()) {
          permissions.add(Permission.ADMIN);
        }
      }
      return permissions;
    } catch (AuthorizationUnavailableException e) {
      return permissions;
    }
  }

  @Override
  public void initializeOwnerAuthorization(User user, String ownerId, String dataSetId)
    throws AuthorizationCreationException {

    String vreId = createCombinedId(ownerId, dataSetId);

    try {
      vreAuthorizationCrud.createAuthorization(vreId, user, "ADMIN");
    } catch (AuthorizationCreationException e) {
      throw e;
    }

  }

  @Override
  public void removeAuthorizations(String ownerId, String vreId) throws PermissionFetchingException {
    Optional<User> user;

    try {
      user = userValidator.getUserFromPersistentId(ownerId);
    } catch (UserValidationException e) {
      throw new PermissionFetchingException(String.format("Could not retrieve User for userId '%s'", ownerId));
    }

    try {
      if (user.isPresent()) {
        vreAuthorizationCrud.deleteVreAuthorizations(vreId, user.get());
      } else {
        throw new PermissionFetchingException(String.format("No User found for userId '%s'", ownerId));
      }
    } catch (AuthorizationException | AuthorizationUnavailableException e) {
      throw new PermissionFetchingException(String.format("Authorization not available for" +
        " userId '%s' .", ownerId));
    }
  }

}
