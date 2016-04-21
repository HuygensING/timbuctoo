package nl.knaw.huygens.timbuctoo.server.databasemigration;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;

public interface DatabaseMigration {

  String getName();

  void applyToVertex(Vertex vertex) throws IOException;
}
