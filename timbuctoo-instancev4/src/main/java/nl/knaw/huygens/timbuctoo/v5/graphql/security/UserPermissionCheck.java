package nl.knaw.huygens.timbuctoo.v5.graphql.security;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class UserPermissionCheck {
  private static final Logger LOG = LoggerFactory.getLogger(UserPermissionCheck.class);
  private final Optional<User> user;
  private final PermissionFetcher permissionFetcher;
  private final HashMap<String, Set<Permission>> dataSetPermissionMap = new HashMap<>();

  public UserPermissionCheck(Optional<User> user, PermissionFetcher permissionFetcher) {
    this.user = user;
    this.permissionFetcher = permissionFetcher;
  }

  public Set<Permission> getPermissions(DataSetMetaData dataSetMetaData) {

    return user.map(user -> {
      try {
        String dataSetId = dataSetMetaData.getCombinedId();
        if (dataSetPermissionMap.containsKey(dataSetId)) {
          return dataSetPermissionMap.get(dataSetId);
        }
        return dataSetPermissionMap.put(dataSetId, permissionFetcher.getPermissions(user, dataSetMetaData));
      } catch (PermissionFetchingException e) {
        LOG.error("Could not fetch permissions for user '{}' on data set '{}'",
          user.getDisplayName(),
          dataSetMetaData.getDataSetId()
        );
        LOG.error("Error thrown", e);
        return Collections.<Permission>emptySet();
      }
    }).orElse(Collections.emptySet());
  }

  public boolean hasPermission(DataSetMetaData dataSet, Permission permission) {
    // do not use permissionFetcher.hasPermission,  getPermissions will cache the permissions of the users for the
    // http request and therefore makes the loading of the schema a lot faster
    return getPermissions(dataSet).contains(permission);
  }
      }
    }
    return false;
  }
}
