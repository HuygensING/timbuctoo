package nl.knaw.huygens.timbuctoo.search.description.sort;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.server.rest.search.SortParameter;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SortDescription {
  private final List<SortFieldDescription> sortFieldDescriptions;

  public SortDescription(List<SortFieldDescription> sortFieldDescriptions) {
    this.sortFieldDescriptions = sortFieldDescriptions;
  }

  public void sort(GraphTraversal<Vertex, Vertex> searchResult, List<SortParameter> sortParameters) {
    if (sortParameters.isEmpty()) {
      return;
    }
    Map<GraphTraversal<?, ?>, Order> traversals = collectTraversals(sortParameters);

    if (traversals.isEmpty()) {
      return;
    }

    searchResult.order();
    for (Map.Entry<GraphTraversal<?, ?>, Order> entry : traversals.entrySet()) {
      searchResult.by(entry.getKey(), entry.getValue());
    }

  }

  private Map<GraphTraversal<?, ?>, Order> collectTraversals(List<SortParameter> sortParameters) {
    Map<GraphTraversal<?, ?>, Order> traversals = Maps.newHashMap();
    sortParameters.forEach(param -> {
      Optional<SortFieldDescription> description =
        sortFieldDescriptions.stream().filter(desc -> Objects.equals(desc.getName(), param.getFieldName())).findFirst();
      if (description.isPresent()) {
        traversals.put(description.get().getTraversal(), param.getDirection().toOrder());
      }
    });

    return traversals;
  }

}
