package nl.knaw.huygens.timbuctoo.v5.permissions;

public class AdminPermissionChecker implements PermissionChecker {
  @Override
  public void satisfy(String key, String value) { }

  @Override
  public boolean hasPermission() {
    return true;
  }

  @Override
  public PermissionChecker split() {
    return this;
  }
}
