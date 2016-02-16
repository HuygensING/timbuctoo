package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.SearchResult;
import nl.knaw.huygens.timbuctoo.search.description.facet.Facet;
import nl.knaw.huygens.timbuctoo.server.rest.search.SearchRequestV2_1;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;

public abstract class AbstractSearchDescription implements SearchDescription {

  public static final SubgraphStrategy LATEST_ONLY =
    SubgraphStrategy.build().vertexCriterion(has("isLatest", true)).create();

  protected List<Facet> createFacets(GraphTraversal<Vertex, Vertex> vertices) {

    return getFacetDescriptions().stream()
                                 .map(facetDescription -> facetDescription.getFacet(vertices.asAdmin().clone()))
                                 .collect(toList());
  }

  protected EntityRef createRef(Vertex vertex) {
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
  public SearchResult execute(Graph graph, SearchRequestV2_1 searchRequest) {
    GraphTraversalSource latestVertices = GraphTraversalSource.build().with(LATEST_ONLY).create(graph);
    GraphTraversal<Vertex, Vertex> vertices = filterByType(latestVertices);
    // filter by facets
    getFacetDescriptions().forEach(desc -> desc.filter(vertices, searchRequest.getFacetValues()));

    GraphTraversal.Admin<Vertex, Vertex> refsClone = vertices.asAdmin().clone();
    List<EntityRef> refs = refsClone.map(vertex -> createRef(vertex.get())).toList();
    List<Facet> facets = createFacets(vertices);

    return new SearchResult(refs, this, facets);
  }

  protected GraphTraversal<Vertex, Vertex> filterByType(GraphTraversalSource traversalSource) {
    return traversalSource.V().filter(x -> ((String) x.get().property("types").value()).contains(getType()));
  }

  // Hooks

  protected abstract List<FacetDescription> getFacetDescriptions();

  protected abstract Map<String, PropertyDescriptor> getDataPropertyDescriptors();

  protected abstract PropertyDescriptor getDisplayNameDescriptor();

  protected abstract PropertyDescriptor getIdDescriptor();

  protected abstract String getType();
}


