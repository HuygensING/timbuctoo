package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.io.IOException;

public interface DatabaseMigration {
  void beforeMigration(GraphWrapper graphManager);

  void execute(GraphWrapper graphWrapper) throws IOException;
}
