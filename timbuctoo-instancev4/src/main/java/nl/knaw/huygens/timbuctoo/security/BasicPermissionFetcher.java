package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData.createCombinedId;

public class BasicPermissionFetcher implements PermissionFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(BasicPermissionFetcher.class);
  private final VreAuthorizationCrud vreAuthorizationCrud;

  public BasicPermissionFetcher(VreAuthorizationCrud vreAuthorizationCrud) {
    this.vreAuthorizationCrud = vreAuthorizationCrud;
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
            if (!isImportedDataSet(dataSetMetadata)) {
              permissions.add(Permission.WRITE);
            }
            permissions.add(Permission.READ);
          }
          if (vreAuthorization.get().hasAdminAccess()) {
            permissions.add(Permission.IMPORT_DATA);
            permissions.add(Permission.REMOVE_DATASET);
            permissions.add(Permission.PUBLISH_DATASET);
            permissions.add(Permission.EDIT_COLLECTION_METADATA);
            permissions.add(Permission.EDIT_DATASET_METADATA);
            permissions.add(Permission.EXTEND_SCHEMA);
            permissions.add(Permission.CONFIG_INDEX);
            permissions.add(Permission.CONFIG_VIEW);
            permissions.add(Permission.CHANGE_SUMMARYPROPS);
            permissions.add(Permission.IMPORT_RESOURCESYNC);
            permissions.add(Permission.UPDATE_RESOURCESYNC);
            permissions.add(Permission.READ);
          }
        }
      }
      return permissions;
    } catch (AuthorizationUnavailableException e) {
      LOG.error("Authorizations unavailable", e);
      return permissions;
    }
  }

  @Override
  public boolean hasPermission(User user, DataSetMetaData dataSet, Permission permission)
    throws PermissionFetchingException {
    return getPermissions(user, dataSet).contains(permission);
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
          permissions.add(Permission.IMPORT_DATA);
          permissions.add(Permission.REMOVE_DATASET);
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
  public void removeAuthorizations(String vreId) throws PermissionFetchingException {
    try {
      vreAuthorizationCrud.deleteVreAuthorizations(vreId);
    } catch (AuthorizationUnavailableException e) {
      throw new PermissionFetchingException(String.format("Delete of authorizations failed for '%s'.", vreId));
    }
  }

  private boolean isImportedDataSet(DataSetMetaData dataSetMetaData) {
    return dataSetMetaData.getImportInfo() != null && dataSetMetaData.getImportInfo().size() > 0;
  }
}
