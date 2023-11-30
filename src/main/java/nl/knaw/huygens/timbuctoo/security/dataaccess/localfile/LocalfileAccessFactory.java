package nl.knaw.huygens.timbuctoo.security.dataaccess.localfile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.security.JsonPermissionConfiguration;
import nl.knaw.huygens.timbuctoo.security.PermissionConfiguration;
import nl.knaw.huygens.timbuctoo.security.healthchecks.DirectoryHealthCheck;
import nl.knaw.huygens.timbuctoo.security.healthchecks.FileHealthCheck;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.security.dataaccess.UserAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.VreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class LocalfileAccessFactory implements AccessFactory {
  private final String authorizationsPath;
  private final String usersFilePath;
  private final String permissionConfig;

  @JsonCreator
  public LocalfileAccessFactory(@JsonProperty("authorizationsPath") String authorizationsPath,
                                @JsonProperty("permissionConfig") String permissionConfig,
                                @JsonProperty("usersFilePath") String usersFilePath) {
    this.authorizationsPath = authorizationsPath;
    this.permissionConfig = permissionConfig;
    this.usersFilePath = usersFilePath;
  }

  @Override
  public Iterator<Tuple<String, Supplier<Optional<String>>>> getHealthChecks() {
    List<Tuple<String, Supplier<Optional<String>>>> list = new ArrayList<>();
    list.add(tuple("authorizations directory available", new DirectoryHealthCheck(Paths.get(authorizationsPath))));
    list.add(tuple("users file available", new FileHealthCheck(Paths.get(usersFilePath))));
    list.add(tuple("permission config available", new FileHealthCheck(Paths.get(permissionConfig))));

    return list.iterator();
  }

  @Override
  public PermissionConfiguration getPermissionConfig() {
    try {
      Path permissionConfigPath = Paths.get(permissionConfig);
      PermissionConfigMigrator permissionConfigMigrator = new PermissionConfigMigrator(permissionConfigPath);
      if (!Files.exists(permissionConfigPath)) {
        permissionConfigMigrator.execute();
      }
      permissionConfigMigrator.update();
      return new JsonPermissionConfiguration(new FileInputStream(permissionConfig));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public UserAccess getUserAccess() {
    try {
      Path userPath = Paths.get(usersFilePath);
      if (!Files.isDirectory(userPath.getParent())) {
        Files.createDirectories(userPath.getParent());
      }
      if (!Files.exists(userPath)) {
        Files.write(userPath, "[]".getBytes());
      }
      return new LocalFileUserAccess(userPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public VreAuthorizationAccess getVreAuthorizationAccess() {
    try {
      Path authorizationsFolder = Paths.get(authorizationsPath);
      if (!Files.isDirectory(authorizationsFolder)) {
        Files.createDirectories(authorizationsFolder);
      }
      return new LocalFileVreAuthorizationAccess(authorizationsFolder);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
