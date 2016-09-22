package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.database.dto.RelationRef;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface CustomRelationProperties {
  void execute(GraphTraversalSource traversalSource, Vre vre, Vertex target, RelationRef relationRef);
}
