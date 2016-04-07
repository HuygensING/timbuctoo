package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;

import java.io.IOException;

public interface DatabaseMigration {

  String getName();

  void execute(TimbuctooConfiguration configuration, GraphWrapper graphWrapper) throws IOException;
}
