package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class MigrateDatabase implements Runnable {
  public static final Logger LOG = LoggerFactory.getLogger(MigrateDatabase.class);
  public static final String EXECUTED_MIGRATIONS_TYPE = "executed-migrations";
  private final List<DatabaseMigration> migrations;
  private final GraphWrapper graphWrapper;

  public MigrateDatabase(GraphWrapper graphWrapper, List<DatabaseMigration> migrations) {
    this.graphWrapper = graphWrapper;
    this.migrations = migrations;
  }

  @Override
  public void run() {
    Graph graph = graphWrapper.getGraph();

    try (Transaction transaction = graph.tx()) {
      List<String> executedMigrations = graph.traversal().V()
              .has("type", EXECUTED_MIGRATIONS_TYPE)
              .map(vertexTraverser -> (String) vertexTraverser.get().property("name").value())
              .toList();


      for (DatabaseMigration migration : migrations) {
        final String name = migration.getName();
        if (!executedMigrations.contains(name)) {
          LOG.info("Executing \"{}\"", name);
          executeMigration(migration, transaction, graphWrapper.getGraph());

          saveExecution(graph, transaction, name);
          LOG.info("Finished executing \"{}\"", name);
        } else {
          LOG.info("Ignoring \"{}\" - already executed", name);
        }

      }
    } catch (IOException e) {
      LOG.error("Migration failed", e);
    }
  }

  public void saveExecution(Graph graph, Transaction transaction, String name) {
    if (!transaction.isOpen()) {
      transaction.open();
    }
    Vertex migrationVertex = graph.addVertex();
    migrationVertex.property("type", EXECUTED_MIGRATIONS_TYPE);
    migrationVertex.property("name", name);
    migrationVertex.property("tim_id", UUID.randomUUID().toString());
    transaction.commit();
  }

  public void executeMigration(DatabaseMigration migration, Transaction transaction, Graph graph) throws IOException {
    if (!transaction.isOpen()) {
      transaction.open();
    }
    migration.beforeMigration((TinkerpopGraphManager) graphWrapper);
    GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V();
    int amount = 0;
    while (traversal.hasNext()) {
      migration.applyToVertex(traversal.next());
      if (++amount > 100) {
        transaction.commit();
        transaction.close();
        transaction.open();
        amount = 0;
        System.out.print(".");
        System.out.flush();
      }
    }
    transaction.commit();
    System.out.println();

    transaction.close();
    migration.generateIndexes((Neo4jGraph) graph, transaction);

  }
}
