package nl.knaw.huygens.timbuctoo.security.dataaccess.localfile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.security.healthchecks.DirectoryHealthCheck;
import nl.knaw.huygens.timbuctoo.security.healthchecks.FileHealthCheck;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AccessNotPossibleException;
import nl.knaw.huygens.timbuctoo.security.dataaccess.LoginAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.UserAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.VreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.slf4j.Logger;

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

  private String authorizationsPath;
  private String loginsFilePath;
  private String usersFilePath;


  @JsonCreator
  public LocalfileAccessFactory(@JsonProperty("authorizationsPath") String authorizationsPath,
                                @JsonProperty("loginsFilePath") String loginsFilePath,
                                @JsonProperty("usersFilePath") String usersFilePath
  ) {
    this.authorizationsPath = authorizationsPath;
    this.loginsFilePath = loginsFilePath;
    this.usersFilePath = usersFilePath;
  }

  @Override
  public Iterator<Tuple<String, Supplier<Optional<String>>>> getHealthChecks() {
    List<Tuple<String, Supplier<Optional<String>>>> list = new ArrayList<>();
    list.add(tuple("login file available", new FileHealthCheck(Paths.get(loginsFilePath))));
    list.add(tuple("authorizations directory available", new DirectoryHealthCheck(Paths.get(authorizationsPath))));
    list.add(tuple("users file available", new FileHealthCheck(Paths.get(usersFilePath))));
    return list.iterator();
  }

  @Override
  public LoginAccess getLoginAccess() throws AccessNotPossibleException {
    Path loginFile = Paths.get(loginsFilePath);
    if (!loginFile.toFile().isFile()) {
      LOG.error("File " + loginFile.toAbsolutePath() + " does not exist");
      throw new AccessNotPossibleException("File does not exist");
    }
    return new LocalFileLoginAccess(loginFile);
  }

  @Override
  public UserAccess getUserAccess() throws AccessNotPossibleException {
    Path userPath = Paths.get(usersFilePath);
    if (!userPath.toFile().isFile()) {
      LOG.error("File " + userPath.toAbsolutePath() + " does not exist");
      throw new AccessNotPossibleException("File does not exist");
    }
    return new LocalFileUserAccess(userPath);
  }

  @Override
  public VreAuthorizationAccess getVreAuthorizationAccess() throws AccessNotPossibleException {
    Path authorizationsFolder = Paths.get(authorizationsPath);
    if (!authorizationsFolder.toFile().isDirectory()) {
      if (!authorizationsFolder.toFile().mkdirs()) {
        LOG.error("Directory " + authorizationsFolder.toAbsolutePath() + " does not exist and cannot be created");
        throw new AccessNotPossibleException("Direcory does not exist");
      }
    }
    return new LocalFileVreAuthorizationAccess(authorizationsFolder);
  }

}
