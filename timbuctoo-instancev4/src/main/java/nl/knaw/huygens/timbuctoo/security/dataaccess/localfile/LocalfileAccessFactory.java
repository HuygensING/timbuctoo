package nl.knaw.huygens.timbuctoo.security.dataaccess.localfile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.security.JsonPermissionConfiguration;
import nl.knaw.huygens.timbuctoo.security.PermissionConfiguration;
import nl.knaw.huygens.timbuctoo.security.healthchecks.DirectoryHealthCheck;
import nl.knaw.huygens.timbuctoo.security.healthchecks.FileHealthCheck;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AccessNotPossibleException;
import nl.knaw.huygens.timbuctoo.security.dataaccess.LoginAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.UserAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.VreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.slf4j.Logger;

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
import static org.slf4j.LoggerFactory.getLogger;

public class LocalfileAccessFactory implements AccessFactory {
  private static final Logger LOG = getLogger(LocalfileAccessFactory.class);
  private final String authorizationsPath;
  private final String loginsFilePath;
  private final String usersFilePath;
  private final String permissionConfig;


  @JsonCreator
  public LocalfileAccessFactory(@JsonProperty("authorizationsPath") String authorizationsPath,
                                @JsonProperty("permissionConfig") String permissionConfig,
                                @JsonProperty("loginsFilePath") String loginsFilePath,
                                @JsonProperty("usersFilePath") String usersFilePath) {
    this.authorizationsPath = authorizationsPath;
    this.permissionConfig = permissionConfig;
    this.loginsFilePath = loginsFilePath;
    this.usersFilePath = usersFilePath;
  }

  @Override
  public Iterator<Tuple<String, Supplier<Optional<String>>>> getHealthChecks() {
    List<Tuple<String, Supplier<Optional<String>>>> list = new ArrayList<>();
    list.add(tuple("login file available", new FileHealthCheck(Paths.get(loginsFilePath))));
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
  public LoginAccess getLoginAccess() {
    try {
      Path loginPath = Paths.get(loginsFilePath);
      if (!Files.isDirectory(loginPath.getParent())) {
        Files.createDirectories(loginPath.getParent());
      }
      if (!Files.exists(loginPath)) {
        Files.write(loginPath, "[]".getBytes());
      }
      return new LocalFileLoginAccess(loginPath);
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
