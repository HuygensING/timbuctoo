package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;

public class FixDcarKeywordDisplayNameMigration implements DatabaseMigration {
  @Override
  public void beforeMigration(GraphWrapper graphManager) {

  }

  @Override
  public void execute(GraphWrapper graphWrapper) throws IOException {
    final Graph graph = graphWrapper.getGraph();
    final Transaction transaction = graph.tx();
    final GraphTraversal<Vertex, Vertex> dcarDisplayNameT = graph.traversal().V()
      .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
      .has("name", "DutchCaribbean")
      .out("hasCollection")
      .has("collectionName", "dcarkeywords")
      .out("hasDisplayName");

    if (!transaction.isOpen()) {
      transaction.open();
    }

    if (dcarDisplayNameT.hasNext()) {
      final Vertex dcarDisplayName = dcarDisplayNameT.next();

      dcarDisplayName.property(LocalProperty.DATABASE_PROPERTY_NAME, "dcarkeyword_value");
    }

    transaction.commit();
    transaction.close();
  }
}
