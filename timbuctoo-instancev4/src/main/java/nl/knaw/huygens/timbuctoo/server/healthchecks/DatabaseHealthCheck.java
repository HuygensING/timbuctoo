package nl.knaw.huygens.timbuctoo.server.healthchecks;

import com.codahale.metrics.health.HealthCheck;

public class DatabaseHealthCheck extends HealthCheck {

  private final DatabaseValidator validator;

  public DatabaseHealthCheck(DatabaseValidator validator) {
    this.validator = validator;
  }

  @Override
  protected Result check() throws Exception {
    if (validator.lazyCheck().isValid()) {
      return Result.healthy();
    } else {
      return Result.unhealthy("Database invalid");
    }
  }
}
