package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery;

import java.util.concurrent.ExecutorService;

public class GraphQlSchemaUpdater {
  private final ExecutorService executorService;
  private final Runnable schemaUpdateCall;

  public GraphQlSchemaUpdater(ExecutorService executorService, Runnable schemaUpdateCall) {
    this.executorService = executorService;
    this.schemaUpdateCall = schemaUpdateCall;
  }

  /**
   * Schedules a schema update
   */
  public void updateSchema() {
    executorService.submit(schemaUpdateCall);
  }
}
