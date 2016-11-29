package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;

import java.io.IOException;

public interface DatabaseMigration {
  void execute(TinkerpopGraphManager graphWrapper) throws IOException;
}
