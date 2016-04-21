package nl.knaw.huygens.timbuctoo.server.healthchecks;

import com.codahale.metrics.health.HealthCheck;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileHealthCheck extends HealthCheck {
  private final Path pathToFile;

  public FileHealthCheck(Path pathToFile) {
    this.pathToFile = pathToFile;
  }

  @Override
  protected Result check() throws Exception {
    if (!Files.isReadable(pathToFile)) {
      return Result.unhealthy("'%s' is not available.", pathToFile.toAbsolutePath());
    }

    if (!Files.isRegularFile(pathToFile)) {
      return Result.unhealthy("'%s' is not a regular file.", pathToFile.toAbsolutePath());
    }

    return Result.healthy();

  }
}
