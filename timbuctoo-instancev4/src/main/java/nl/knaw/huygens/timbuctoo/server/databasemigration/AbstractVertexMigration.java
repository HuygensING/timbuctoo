package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;

public abstract class AbstractVertexMigration implements DatabaseMigration {

  public abstract void applyToVertex(Vertex vertex) throws IOException;

  @Override
  public void beforeMigration(GraphWrapper graphManager) {

  }

  @Override
  public void execute(GraphWrapper graphWrapper) throws IOException {
    Graph graph = graphWrapper.getGraph();
    Transaction transaction = graph.tx();
    if (!transaction.isOpen()) {
      transaction.open();
    }
    this.beforeMigration(graphWrapper);
    GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V();
    int amount = 0;
    while (traversal.hasNext()) {
      Vertex vertex = traversal.next();
      this.applyToVertex(vertex);

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
  }
}
