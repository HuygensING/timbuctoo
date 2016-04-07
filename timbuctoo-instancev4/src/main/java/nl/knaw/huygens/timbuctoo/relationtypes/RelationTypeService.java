package nl.knaw.huygens.timbuctoo.relationtypes;


import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class RelationTypeService {
  private final GraphWrapper graphWrapper;

  public RelationTypeService(GraphWrapper wrapper) {
    this.graphWrapper = wrapper;
  }

  public List<RelationTypeDescription> get(String name) {
    return graphWrapper.getGraph().traversal().V().has("relationtype_regularName").toList().stream()
            .map(RelationTypeDescription::new)
            .collect(toList());
  }
}
