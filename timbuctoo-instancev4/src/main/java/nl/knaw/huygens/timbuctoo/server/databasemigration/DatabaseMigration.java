package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;

public interface DatabaseMigration {

  String getName();

  void generateIndexes(Neo4jGraph neo4jGraph, Transaction transaction);

  void beforeMigration(TinkerpopGraphManager graphManager);

  void applyToVertex(Vertex vertex) throws IOException;
}
