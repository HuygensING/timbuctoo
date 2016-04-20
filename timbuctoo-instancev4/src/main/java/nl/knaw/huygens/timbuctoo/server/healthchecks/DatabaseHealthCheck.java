package nl.knaw.huygens.timbuctoo.server.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHealthCheck extends HealthCheck {

  private final DatabaseValidator validator;

  public DatabaseHealthCheck(GraphWrapper graphWrapper, int timeoutInHours, List<DatabaseCheck> databaseChecks) {
    validator = new DatabaseValidator(graphWrapper, timeoutInHours, Clock.systemUTC(), databaseChecks);
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
