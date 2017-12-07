package nl.knaw.huygens.timbuctoo.v5.graphql.security;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class UserPermissionCheck {
  private final Optional<User> user;
  private final PermissionFetcher permissionFetcher;
  private final Set<Permission> defaultPermissions;

  public UserPermissionCheck(Optional<User> user, PermissionFetcher permissionFetcher,
                             Set<Permission> defaultPermissions) {
    this.user = user;
    this.permissionFetcher = permissionFetcher;
    this.defaultPermissions = defaultPermissions;
  }

  public Set<Permission> getPermissions(DataSetMetaData dataSetMetaData) {
    Set<Permission> permissions = user
      .map(user -> {
        try {
          return permissionFetcher.getPermissions(user.getPersistentId(), dataSetMetaData);
        } catch (PermissionFetchingException e) {
          return Collections.<Permission>emptySet();
        }
      })
      .orElse(
        Sets.newHashSet()
      );

    if (permissions.isEmpty() && dataSetMetaData.isPublished()) {
      permissions = defaultPermissions;
    }
    return permissions;
  }
}
