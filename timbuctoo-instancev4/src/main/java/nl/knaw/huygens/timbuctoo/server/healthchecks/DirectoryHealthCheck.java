package nl.knaw.huygens.timbuctoo.server.healthchecks;

import com.codahale.metrics.health.HealthCheck;

import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryHealthCheck extends HealthCheck {
  private final Path pathToDirectory;

  public DirectoryHealthCheck(Path pathToDirectory) {
    this.pathToDirectory = pathToDirectory;
  }

  @Override
  protected Result check() throws Exception {
    if (!Files.isReadable(pathToDirectory)) {
      return Result.unhealthy("'%s' is not available.", pathToDirectory.toAbsolutePath());
    }

    if (!Files.isDirectory(pathToDirectory)) {
      return Result.unhealthy("'%s' is not a directory.", pathToDirectory.toAbsolutePath());
    }

    if (!Files.isWritable(pathToDirectory)) {
      return Result.unhealthy("'%s' is not writable.", pathToDirectory.toAbsolutePath());
    }

    return Result.healthy();

  }
}
