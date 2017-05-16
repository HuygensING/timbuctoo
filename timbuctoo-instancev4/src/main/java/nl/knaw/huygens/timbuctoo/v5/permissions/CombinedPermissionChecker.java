package nl.knaw.huygens.timbuctoo.v5.permissions;

import com.fasterxml.jackson.annotation.JsonCreator;

public final class CombinedPermissionChecker implements PermissionChecker {

  private final PermissionChecker[] permissionCheckers;

  @JsonCreator
  public CombinedPermissionChecker(PermissionChecker... permissionCheckers) {
    this.permissionCheckers = permissionCheckers;
  }

  @Override
  public void satisfy(String key, String value) {
    for (PermissionChecker permissionChecker : permissionCheckers) {
      permissionChecker.satisfy(key, value);
    }
  }

  @Override
  public boolean hasPermission() {
    for (PermissionChecker permissionChecker : permissionCheckers) {
      if (permissionChecker.hasPermission()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public PermissionChecker split() {
    PermissionChecker[] clones = new PermissionChecker[permissionCheckers.length];
    for (int i = 0; i < permissionCheckers.length; i++) {
      final PermissionChecker permissionChecker = permissionCheckers[i];
      clones[i] = permissionChecker.split();
    }
    return new CombinedPermissionChecker(clones);
  }
}
