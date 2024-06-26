package nl.knaw.huygens.timbuctoo.security.dataaccess.localfile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.security.JsonPermissionConfiguration.RolePermissions;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class PermissionConfigMigrator {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Path permissionConfig;

  public PermissionConfigMigrator(Path permissionConfig) {
    this.permissionConfig = permissionConfig;
  }

  public void execute() throws IOException {
    // Has a semantic connection with
    // nl.knaw.huygens.timbuctoo.graphql.security.UserPermissionCheck.OldGraphQlPermission
    LoggerFactory.getLogger(PermissionConfigMigrator.class).info("Setting up a minimal permission configuration");
    String permissions =
        """
            [
              {
                "roleName": "USER",
                "permissions": [
                  "READ",
                  "WRITE",
                  "READ_IMPORT_STATUS"
                ]
              },
              {
                "roleName": "ADMIN",
                "permissions": [
                  "IMPORT_DATA",
                  "REMOVE_DATASET",
                  "PUBLISH_DATASET",
                  "EDIT_COLLECTION_METADATA",
                  "EDIT_DATASET_METADATA",
                  "EXTEND_SCHEMA",
                  "CONFIG_INDEX",
                  "CONFIG_VIEW",
                  "CHANGE_SUMMARYPROPS",
                  "READ",
                  "WRITE",
                  "UPDATE_RESOURCESYNC",
                  "IMPORT_RESOURCESYNC",
                  "READ_IMPORT_STATUS"
                ]
              }
            ]""";

    Files.write(permissionConfig, permissions.getBytes());
  }

  public void update() throws IOException {
    Set<RolePermissions> rolePermissions = OBJECT_MAPPER.readValue(
      new FileInputStream(permissionConfig.toFile()), new TypeReference<>() { });

    addCreateDeletePermissions(rolePermissions);
    addCustomProvenancePermission(rolePermissions);

    OBJECT_MAPPER.writeValue(permissionConfig.toFile(), rolePermissions);
  }

  private void addCreateDeletePermissions(Set<RolePermissions> rolePermissions) {
    rolePermissions.forEach(rolePermission -> {
      if (rolePermission.roleName.equals("USER") || rolePermission.roleName.equals("ADMIN")) {
        rolePermission.permissions.add(Permission.CREATE);
        rolePermission.permissions.add(Permission.DELETE);
      }
    });
  }

  private void addCustomProvenancePermission(Set<RolePermissions> rolePermissions) {
    rolePermissions.forEach(rolePermission -> {
      if (rolePermission.roleName.equals("USER") || rolePermission.roleName.equals("ADMIN")) {
        rolePermission.permissions.add(Permission.SET_CUSTOM_PROV);
      }
    });
  }
}
