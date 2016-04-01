package nl.knaw.huygens.timbuctoo.search.description;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.SearchDescription;
import nl.knaw.huygens.timbuctoo.search.SearchResult;
import nl.knaw.huygens.timbuctoo.search.description.facet.Facet;
import nl.knaw.huygens.timbuctoo.search.description.fulltext.FullTextSearchDescription;
import nl.knaw.huygens.timbuctoo.search.description.sort.SortDescription;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.SearchRequestV2_1;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public abstract class AbstractSearchDescription implements SearchDescription {

  public static final SortDescription NO_OP_SORT_DESCRIPTION = new SortDescription(Lists.newArrayList());

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
  public SearchResult execute(GraphWrapper graphWrapper, SearchRequestV2_1 searchRequest) {
    GraphTraversalSource latestStage = graphWrapper.getLatestState();
    GraphTraversal<Vertex, Vertex> vertices = filterByType(latestStage);
    // filter by facets
    getFacetDescriptions().forEach(desc -> desc.filter(vertices, searchRequest.getFacetValues()));
    // filter by full text search
    searchRequest.getFullTextSearchParameters().forEach(param -> {
      Optional<FullTextSearchDescription> first = getFullTextSearchDescriptions()
              .stream()
              .filter(desc -> Objects.equals(param.getName(), desc.getName()))
              .findFirst();
      if (first.isPresent()) {
        first.get().filter(vertices, param);
      }
    });
    // order / sort
    getSortDescription().sort(vertices, searchRequest.getSortParameters());
    Map<String, Map<String, Set<Vertex>>> facetCounts = new HashMap<>();
    Map<String, FacetDescription> facetDescriptionMap = new HashMap<>();

    List<Vertex> searchResult = vertices.map(vertexTraverser -> {
      getFacetDescriptions().stream().forEach(facetDescription -> {
        // These facets are only used for filtering
        if (getOnlyFilterFacetList().contains(facetDescription.getName())) {
          return;
        }

        if (!facetCounts.containsKey(facetDescription.getName())) {
          facetCounts.put(facetDescription.getName(), new HashMap<>());
          facetDescriptionMap.put(facetDescription.getName(), facetDescription);
        }

        final List<String> facetValues = facetDescription.getValues(vertexTraverser.get());
        if (facetValues != null) {
          Map<String, Set<Vertex>> counts = facetCounts.get(facetDescription.getName());
          facetValues.stream().forEach(facetValue -> {
            if (!counts.containsKey(facetValue)) {
              counts.put(facetValue, new HashSet<>());
            }
            Set<Vertex> bag = counts.get(facetValue);
            bag.add(vertexTraverser.get());
          });
        }
      });
      return vertexTraverser.get();
    }).toList();

    List<Facet> facets = facetCounts.entrySet().stream()
            .map((entry) -> facetDescriptionMap.get(entry.getKey()).getFacet(entry.getValue()))
            .collect(toList());

    return new SearchResult(searchResult, this, facets);
  }

  /**
   * A method to create a new GraphTraversal, because cloning is to slow.
   * It creates a new graph with a list created of the vertices.
   * It creates an empty graph if vertices does not contain any vertices.
   */
  protected GraphTraversal<Vertex, Vertex> getSearchResult(GraphWrapper graphWrapper, List<Vertex> vertexList) {
    if (vertexList.isEmpty()) {
      return EmptyGraph.instance().traversal().V();
    }
    return graphWrapper.getGraph().traversal().V(vertexList);

  }

  protected GraphTraversal<Vertex, Vertex> filterByType(GraphTraversalSource traversalSource) {
    return traversalSource.V().filter(
      x -> ((String) x.get().property("types").value()).contains("\"" + getType() + "\"")
    );
  }

  protected List<String> getOnlyFilterFacetList() {
    return Lists.newArrayList();
  }

  // Hooks

  protected abstract List<FacetDescription> getFacetDescriptions();

  protected abstract Map<String, PropertyDescriptor> getDataPropertyDescriptors();

  protected abstract PropertyDescriptor getDisplayNameDescriptor();

  protected abstract PropertyDescriptor getIdDescriptor();

  protected abstract String getType();

  protected abstract List<FullTextSearchDescription> getFullTextSearchDescriptions();

  protected SortDescription getSortDescription() {
    return NO_OP_SORT_DESCRIPTION;
  }
}


