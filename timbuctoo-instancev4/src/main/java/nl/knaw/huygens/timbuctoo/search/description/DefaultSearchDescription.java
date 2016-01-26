package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.TimbuctooQuery;
import nl.knaw.huygens.timbuctoo.search.description.facet.Facet;
import nl.knaw.huygens.timbuctoo.server.rest.search.SearchRequestV2_1;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

class DefaultSearchDescription implements SearchDescription {
  private final List<String> sortableFields;

  private final List<String> fullTextSearchFields;
  private final String type;
  private final PropertyDescriptor idDescriptor;
  private List<FacetDescription> facetDescriptions;
  private final Map<String, PropertyDescriptor> dataPropertyDescriptors;
  private final PropertyDescriptor displayNameDescriptor;

  public DefaultSearchDescription(PropertyDescriptor idDescriptor, PropertyDescriptor displayNameDescriptor,
                                  List<FacetDescription> facetDescriptions,
                                  Map<String, PropertyDescriptor> dataPropertyDescriptors,
                                  List<String> sortableFields, List<String> fullTextSearchFields,
                                  String type) {
    this.facetDescriptions = facetDescriptions;
    this.dataPropertyDescriptors = dataPropertyDescriptors;
    this.displayNameDescriptor = displayNameDescriptor;
    this.idDescriptor = idDescriptor;
    this.sortableFields = sortableFields;
    this.fullTextSearchFields = fullTextSearchFields;
    this.type = type;
  }

  @Override
  public List<String> getSortableFields() {
    return sortableFields;
  }

  @Override
  public List<String> getFullTextSearchFields() {
    return fullTextSearchFields;
  }

  @Override
  public TimbuctooQuery createQuery(SearchRequestV2_1 searchRequest) {
    return new TimbuctooQuery(this);
  }

  @Override
  public List<Facet> createFacets(List<Vertex> vertices) {

    return facetDescriptions.stream().map(facetDescription -> facetDescription.getFacet(vertices)).collect(toList());
  }

  @Override
  public EntityRef createRef(Vertex vertex) {
    String id = idDescriptor.get(vertex);

    EntityRef ref = new EntityRef(type, id);

    String displayName = displayNameDescriptor.get(vertex);
    ref.setDisplayName(displayName);

    Map<String, Object> data = Maps.newHashMap();

    dataPropertyDescriptors.entrySet().forEach(entry -> data.put(entry.getKey(), entry.getValue().get(vertex)));

    ref.setData(data);

    return ref;
  }

  @Override
  public GraphTraversal<Vertex, Vertex> filterByType(GraphTraversal<Vertex, Vertex> vertices) {
    return vertices.filter(x -> ((String) x.get().property("types").value()).contains(type));
  }

  String getType() {
    return type;
  }

}
