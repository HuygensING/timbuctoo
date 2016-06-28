package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

public class LocationNamesToLocationNameDatabaseMigration extends AbstractVertexMigration {

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
