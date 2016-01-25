package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.TimbuctooQuery;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import nl.knaw.huygens.timbuctoo.server.rest.search.SearchRequestV2_1;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

public class WwDocumentSearchDescription implements SearchDescription {
  private static final List<String> SORTABLE_FIELDS = Lists.newArrayList(
      "dynamic_sort_title",
      "dynamic_k_modified",
      "dynamic_sort_creator");

  private static final List<String> FULL_TEXT_SEARCH_FIELDS = Lists.newArrayList(
      "dynamic_t_author_name",
      "dynamic_t_title",
      "dynamic_t_notes");

  private final PropertyDescriptorFactory propertyDescriptorFactory;

  private final String type = "wwdocument";

  public WwDocumentSearchDescription() {
    propertyDescriptorFactory = new PropertyDescriptorFactory();
  }

  @Override
  public List<String> getSortableFields() {
    return SORTABLE_FIELDS;
  }

  @Override
  public List<String> getFullTextSearchFields() {
    return FULL_TEXT_SEARCH_FIELDS;
  }

  @Override
  public TimbuctooQuery createQuery(SearchRequestV2_1 searchRequest) {
    return new TimbuctooQuery(this);
  }

  @Override
  public EntityRef createRef(Vertex vertex) {
    String id =
        propertyDescriptorFactory.getLocal(ID_DB_PROP, new PropertyParserFactory().getParser(String.class)).get(vertex);

    EntityRef ref = new EntityRef(type, id);

    return ref;
  }

  @Override
  public GraphTraversal<Vertex, Vertex> filterByType(GraphTraversal<Vertex, Vertex> vertices) {
    return vertices.filter(x -> ((String) x.get().property("types").value()).contains(type));
  }

  public String getType() {
    return type;
  }
}
