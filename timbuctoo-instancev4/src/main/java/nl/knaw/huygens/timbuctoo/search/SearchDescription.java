package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.timbuctoo.server.rest.search.SearchRequestV2_1;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

public interface SearchDescription {
  String ID_DB_PROP = "tim_id";

  List<String> getSortableFields();

  List<String> getFullTextSearchFields();

  TimbuctooQuery createQuery(SearchRequestV2_1 searchRequest);

  EntityRef createRef(Vertex vertex);

  GraphTraversal<Vertex, Vertex> filterByType(GraphTraversal<Vertex, Vertex> vertices);
}
