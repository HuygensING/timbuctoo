package nl.knaw.huygens.timbuctoo.security.dataaccess.localfile;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PermissionConfigMigrator {
  private final Path permissionConfig;

  public PermissionConfigMigrator(Path permissionConfig) {
    this.permissionConfig = permissionConfig;
  }

  public void execute() throws IOException {
    // Has a semantic connection with
    // nl.knaw.huygens.timbuctoo.v5.graphql.security.UserPermissionCheck.OldGraphQlPermission
    LoggerFactory.getLogger(PermissionConfigMigrator.class).info("Setting up a minimal permission configuration");
    String permissions =
      "[\n" +
      "  {\n" +
      "    \"roleName\": \"USER\",\n" +
      "    \"permissions\": [\n" +
      "      \"READ\",\n" +
      "      \"WRITE\",\n" +
      "      \"READ_IMPORT_STATUS\"\n" +
      "    ]\n" +
      "  },\n" +
      "  {\n" +
      "    \"roleName\": \"ADMIN\",\n" +
      "    \"permissions\": [\n" +
      "      \"IMPORT_DATA\",\n" +
      "      \"REMOVE_DATASET\",\n" +
      "      \"PUBLISH_DATASET\",\n" +
      "      \"EDIT_COLLECTION_METADATA\",\n" +
      "      \"EDIT_DATASET_METADATA\",\n" +
      "      \"EXTEND_SCHEMA\",\n" +
      "      \"CONFIG_INDEX\",\n" +
      "      \"CONFIG_VIEW\",\n" +
      "      \"CHANGE_SUMMARYPROPS\",\n" +
      "      \"READ\",\n" +
      "      \"WRITE\",\n" +
      "      \"UPDATE_RESOURCESYNC\",\n" +
      "      \"IMPORT_RESOURCESYNC\",\n" +
      "      \"READ_IMPORT_STATUS\"\n" +
      "    ]\n" +
      "  }\n" +
      "]";

    Files.write(permissionConfig, permissions.getBytes());
  }
}