package nl.knaw.huygens.timbuctoo.dropwizard.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.graphql.collectionfilter.CollectionFilter;

public class CollectionFilterCheck extends HealthCheck {
  private final CollectionFilter collectionFilter;

  public CollectionFilterCheck(CollectionFilter collectionFilter) {
    this.collectionFilter = collectionFilter;
  }

  @Override
  protected Result check() throws Exception {
    Tuple<Boolean, String> isHealthyTuple = collectionFilter.isHealthy();

    if (!isHealthyTuple.left()) {
      return Result.unhealthy(isHealthyTuple.right());
    }

    return Result.healthy();
  }
}
