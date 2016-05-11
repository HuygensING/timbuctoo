package nl.knaw.huygens.timbuctoo.server.healthchecks;

import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

public class DatabaseValidator {
  private DatabaseCheck[] databaseChecks;

  public DatabaseValidator(DatabaseCheck... databaseChecks) {
    this.databaseChecks = databaseChecks;
  }

  public ValidationResult check(Graph graph) {
    GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V();
    List<ValidationResult> validationResults = Lists.newArrayList();
    while (traversal.hasNext()) {
      Vertex vertex = traversal.next();
      for (DatabaseCheck databaseCheck : databaseChecks) {
        validationResults.add(databaseCheck.check(vertex));
      }
    }

    return new CompositeValidationResult(validationResults);
  }

}
