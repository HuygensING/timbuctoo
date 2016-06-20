package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

public class LocationNamesToLocationNameDatabaseMigration implements DatabaseMigration {
  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public void generateIndexes(Neo4jGraph neo4jGraph, Transaction transaction) {
    // This task does not create new indexes
  }

  @Override
  public void beforeMigration(TinkerpopGraphManager graphManager) {
    // before hook not needed
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    List<String> entityTypesOrDefault = Lists.newArrayList(getEntityTypesOrDefault(vertex));
    if (entityTypesOrDefault.contains("location")) {
      LocationNames locationNames = new ObjectMapper().readValue((String) vertex.property("names").value(),
        LocationNames.class);

      String name = locationNames.getDefaultName();
      vertex.property("location_name", name == null ? "" : name);
    }
  }
}
