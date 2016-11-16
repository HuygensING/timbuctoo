package nl.knaw.huygens.timbuctoo.server.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import nl.knaw.huygens.timbuctoo.handle.HandleService;

public class HandleServiceStartedCheck extends HealthCheck {
  private final HandleService handleService;

  public HandleServiceStartedCheck(HandleService handleService) {
    this.handleService = handleService;
  }

  @Override
  protected Result check() throws Exception {
    if (handleService.isStarted()) {
      return Result.healthy();
    }

    return Result.unhealthy("HandleService should be started.");

  }
}
