package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;

public interface VertexMigration {

  String getName();

  void beforeMigration(TinkerpopGraphManager graphManager);

  void applyToVertex(Vertex vertex) throws IOException;
}
