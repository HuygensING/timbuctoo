package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
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
  public void beforeMigration(TinkerpopGraphManager graphManager) {
    // before hook not needed
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    List<String> entityTypes = Lists.newArrayList(getEntityTypesOrDefault(vertex));
    if (entityTypes.contains("location")) {
      LocationNames locationNames = new ObjectMapper().readValue((String) vertex.property("names").value(),
        LocationNames.class);

      String name = locationNames.getDefaultName();
      for (String entityType : entityTypes) {
        vertex.property(String.format("%s_name", entityType), name == null ? "" : name);
      }
    }
  }
}
