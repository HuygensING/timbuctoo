package nl.knaw.huygens.timbuctoo.server.rest;

import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.stream.Collectors;

public class WwPersonSearchDescription {
  private static final List<String> SORTABLE_FIELDS = Lists.newArrayList(
    "dynamic_k_modified",
    "dynamic_k_birthDate",
    "dynamic_sort_name",
    "dynamic_k_deathDate");

  private static final List<String> FULL_TEXT_SEARCH_FIELDS = Lists.newArrayList(
    "dynamic_t_tempspouse",
    "dynamic_t_notes",
    "dynamic_t_name");
  private final String type = "wwperson";

  public List<String> getSortableFields() {
    return SORTABLE_FIELDS;
  }

  public List<String> getFullTextSearchFields() {
    return FULL_TEXT_SEARCH_FIELDS;
  }

  public TimbuctooQuery createQuery(SearchRequestV2_1 searchRequest) {
    return new TimbuctooQuery(this);
  }

  public List<EntityRef> createRefs(List<Vertex> vertices) {
    return vertices.stream().map(vertex -> new EntityRef(type, vertex.<String>property("tim_id").value()))
                   .collect(Collectors.toList());
  }

  public GraphTraversal<Vertex, Vertex> filterByType(GraphTraversal<Vertex, Vertex> vertices) {
    return vertices.filter(x -> ((String) x.get().property("types").value()).contains(type));
  }
}
