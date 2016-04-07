package nl.knaw.huygens.timbuctoo.server;

import com.codahale.metrics.health.HealthCheck;
import nl.knaw.huygens.timbuctoo.model.DatabaseInvariantValidator;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;

import java.time.Clock;

public class DatabaseInvariantsHealthCheck extends HealthCheck {

  private final DatabaseInvariantValidator validator;

  public DatabaseInvariantsHealthCheck(GraphWrapper graphWrapper, int timeoutInHours, Vres vres) {
    validator = new DatabaseInvariantValidator(graphWrapper, timeoutInHours, Clock.systemUTC(), vres);
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
