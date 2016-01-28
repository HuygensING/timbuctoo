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

public abstract class AbstractSearchDescription implements SearchDescription {
  @Override
  public TimbuctooQuery createQuery(SearchRequestV2_1 searchRequest) {
    return new TimbuctooQuery(this);
  }

  @Override
  public List<Facet> createFacets(List<Vertex> vertices) {

    return getFacetDescriptions().stream().map(facetDescription -> facetDescription.getFacet(vertices))
                                 .collect(toList());
  }

  @Override
  public EntityRef createRef(Vertex vertex) {
    String id = getIdDescriptor().get(vertex);

    EntityRef ref = new EntityRef(getType(), id);

    String displayName = getDisplayNameDescriptor().get(vertex);
    ref.setDisplayName(displayName);

    Map<String, Object> data = Maps.newHashMap();

    getDataPropertyDescriptors().entrySet().forEach(entry -> data.put(entry.getKey(), entry.getValue().get(vertex)));

    ref.setData(data);

    return ref;
  }

  @Override
  public GraphTraversal<Vertex, Vertex> filterByType(GraphTraversal<Vertex, Vertex> vertices) {
    return vertices.filter(x -> ((String) x.get().property("types").value()).contains(getType()));
  }

  protected abstract List<FacetDescription> getFacetDescriptions();

  protected abstract Map<String, PropertyDescriptor> getDataPropertyDescriptors();

  protected abstract PropertyDescriptor getDisplayNameDescriptor();

  protected abstract PropertyDescriptor getIdDescriptor();

  protected abstract String getType();
}
