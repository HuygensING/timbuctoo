package nl.knaw.huygens.timbuctoo.server.databasemigration;

import javaslang.control.Try;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;

public class LabelDatabaseMigration implements DatabaseMigration {


  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    Neo4jVertex neo4jVertex = (Neo4jVertex) vertex;
    if (neo4jVertex.property("types").isPresent()) {
      String[] types = getEntityTypes(vertex)
              .orElseGet(() -> Try.success(new String[0]))
              .getOrElse(() -> new String[0]);

      for (String type : types) {
        neo4jVertex.addLabel(type);
      }
    }
  }
}
