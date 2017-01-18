package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.database.tinkerpop.Neo4jIndexHandler;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;

import java.io.IOException;
import java.util.UUID;

public class IndexRelationsById implements DatabaseMigration {
  @Override
  public void execute(TinkerPopGraphManager graphWrapper) throws IOException {
    Neo4jIndexHandler neo4jIndexHandler = new Neo4jIndexHandler(graphWrapper);
    graphWrapper.getGraph().traversal().E().has("tim_id").has("isLatest", true).forEachRemaining(e -> {
        neo4jIndexHandler.upsertIntoEdgeIdIndex(UUID.fromString(e.value("tim_id")), e);
      }
    );
  }
}
