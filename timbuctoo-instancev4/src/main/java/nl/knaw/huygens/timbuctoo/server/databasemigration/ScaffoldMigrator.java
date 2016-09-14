package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.databasemigration.scaffold.ScaffoldVresConfig;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;

public class ScaffoldMigrator {
  private static final Logger LOG = LoggerFactory.getLogger(ScaffoldMigrator.class);


  private final GraphWrapper graphWrapper;

  public ScaffoldMigrator(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  public void execute() {
    Graph graph = graphWrapper.getGraph();
    //The migrations are executed first, so those vertices _will_ be present, even on a new empty database
    //The code below will add vertices, so a second launch will not run this code
    boolean databaseHasNonMigrationNodes = graph
      .traversal().V()
      .not(__.has("type", DatabaseMigrator.EXECUTED_MIGRATIONS_TYPE))
      .hasNext();

    if (!databaseHasNonMigrationNodes) {
      LOG.info("Setting up a new scaffold for empty database");
      try {
        new HuygensIngConfigToDatabaseMigration(ScaffoldVresConfig.mappings).execute(graphWrapper);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      final Vertex hasBirthPlaceVertex = graph.addVertex("relationtype");
      hasBirthPlaceVertex.property("rev", 1);
      hasBirthPlaceVertex.property("types", jsnA(jsn("relationtype")).toString());
      hasBirthPlaceVertex.property("tim_id", UUID.randomUUID().toString());

      hasBirthPlaceVertex.property("relationtype_reflexive", false);
      hasBirthPlaceVertex.property("relationtype_symmetric", false);
      hasBirthPlaceVertex.property("isLatest", true);
      hasBirthPlaceVertex.property("relationtype_derived", false);

      hasBirthPlaceVertex.property("relationtype_targetTypeName", "location");
      hasBirthPlaceVertex.property("relationtype_sourceTypeName", "person");
      hasBirthPlaceVertex.property("relationtype_inverseName", "isBirthPlaceOf");
      hasBirthPlaceVertex.property("relationtype_regularName", "hasBirthPlace");

      final Vertex hasDeathPlaceVertex = graph.addVertex("relationtype");
      hasDeathPlaceVertex.property("rev", 1);
      hasDeathPlaceVertex.property("types", jsnA(jsn("relationtype")).toString());
      hasDeathPlaceVertex.property("tim_id", UUID.randomUUID().toString());

      hasDeathPlaceVertex.property("relationtype_reflexive", false);
      hasDeathPlaceVertex.property("relationtype_symmetric", false);
      hasDeathPlaceVertex.property("isLatest", true);
      hasDeathPlaceVertex.property("relationtype_derived", false);

      hasDeathPlaceVertex.property("relationtype_targetTypeName", "location");
      hasDeathPlaceVertex.property("relationtype_sourceTypeName", "person");
      hasDeathPlaceVertex.property("relationtype_inverseName", "isDeathPlaceOf");
      hasDeathPlaceVertex.property("relationtype_regularName", "hasDeathPlace");

      graph.tx().commit();
    }
  }
}
