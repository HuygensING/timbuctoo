package nl.knaw.huygens.timbuctoo.server.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;
import java.util.Set;

public class LabelsAddedToDatabaseHealthCheck extends HealthCheck {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private GraphWrapper graphWrapper;

  public LabelsAddedToDatabaseHealthCheck(TinkerpopGraphManager graphManager) {
    graphWrapper = graphManager;
  }

  @Override
  protected Result check() throws Exception {
    GraphTraversal<Vertex, Vertex> traversal = graphWrapper.getGraph().traversal().V();
    Map<String, Set<String>> failures = Maps.newHashMap();
    while (traversal.hasNext()) {
      Neo4jVertex vertex = (Neo4jVertex) traversal.next();
      if (vertex.property("types").isPresent()) {
        Set<String> types = OBJECT_MAPPER.readValue((String) vertex.property("types").value(), Set.class);
        Sets.SetView<String> difference = Sets.difference(types, vertex.labels());

        if (!difference.isEmpty()) {
          failures.put(vertex.<String>property("tim_id").value(), difference);
        }
      }
    }
    if (failures.isEmpty()) {
      return Result.healthy();
    } else {
      return Result.unhealthy(createMessage(failures));
    }
  }

  private String createMessage(Map<String, Set<String>> failures) {
    StringBuilder stringBuilder = new StringBuilder();
    for (Map.Entry<String, Set<String>> failure : failures.entrySet()) {
      stringBuilder.append(
              String.format("Vertex with tim_id %s misses labels %s\n", failure.getKey(), failure.getValue()));
    }
    return stringBuilder.toString();
  }
}
