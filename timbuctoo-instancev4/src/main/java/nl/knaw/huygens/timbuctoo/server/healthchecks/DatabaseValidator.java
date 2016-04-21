package nl.knaw.huygens.timbuctoo.server.healthchecks;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DatabaseValidator {
  private final GraphWrapper graphWrapper;
  private final int timeoutInHours;
  private final Clock clock;
  private List<DatabaseCheck> databaseChecks;
  private ValidationResult previousResult;
  private Instant lastCheck;

  public DatabaseValidator(GraphWrapper graphWrapper, int timeoutInHours, Clock clock,
                           List<DatabaseCheck> databaseChecks) {
    this.graphWrapper = graphWrapper;
    this.timeoutInHours = timeoutInHours;
    this.clock = clock;
    this.databaseChecks = databaseChecks;
    this.previousResult = null;
    this.lastCheck = Instant.MIN;
  }

  /**
   * Forced check will always recheck regardless of the timeout. This method is blocking.
   *
   * @return whether the database meets its invariants
   */
  ValidationResult check() {
    GraphTraversal<Vertex, Vertex> traversal = graphWrapper.getGraph().traversal().V();
    List<ValidationResult> validationResults = Lists.newArrayList();
    while (traversal.hasNext()) {
      Vertex vertex = traversal.next();
      for (DatabaseCheck databaseCheck : databaseChecks) {
        validationResults.add(databaseCheck.check(vertex));
      }
    }

    return new CompositeValidationResult(validationResults);
  }

  /**
   * Gentle check won't run more often then the delay that is specified in the constructor.
   *
   * @return whether the database meets its invariants.
   */
  public ValidationResult lazyCheck() {
    if (previousResult == null || ChronoUnit.HOURS.between(lastCheck, clock.instant()) >= timeoutInHours) {
      previousResult = check();
      lastCheck = clock.instant();
    }
    return previousResult;
  }

}
