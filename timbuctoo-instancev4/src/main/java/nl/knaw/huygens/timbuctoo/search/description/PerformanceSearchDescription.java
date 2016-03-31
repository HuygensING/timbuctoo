package nl.knaw.huygens.timbuctoo.search.description;

import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.SearchResult;
import nl.knaw.huygens.timbuctoo.search.description.facet.Facet;
import nl.knaw.huygens.timbuctoo.search.description.fulltext.FullTextSearchDescription;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.SearchRequestV2_1;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class PerformanceSearchDescription extends AbstractSearchDescription {

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

    GraphTraversal<Vertex, Vertex> searchResult = getSearchResult(graphWrapper, vertices.toList());

    List<EntityRef> refs = new ArrayList<>();
    Map<String, Map<String, Set<Vertex>>> facetCounts = new HashMap<>();
    Map<String, FacetDescription> facetDescriptionMap = new HashMap<>();

    searchResult.map(vertexTraverser -> {
      getFacetDescriptions().stream().forEach(facetDescription -> {
        if(!facetCounts.containsKey(facetDescription.getName())) {
          facetCounts.put(facetDescription.getName(), new HashMap<>());
          facetDescriptionMap.put(facetDescription.getName(), facetDescription);
        }
        final List<String> facetValues = facetDescription.getValues(vertexTraverser.get());
        if(facetValues != null) {
          Map<String, Set<Vertex>> counts = facetCounts.get(facetDescription.getName());
          facetValues.stream().forEach(facetValue -> {
            if(!counts.containsKey(facetValue)) {
              counts.put(facetValue, new HashSet<>());
            }
            Set<Vertex> bag = counts.get(facetValue);
            bag.add(vertexTraverser.get());
          });
        }
      });
      return vertexTraverser;
    }).toList();


    List<Facet> facets = new ArrayList<>();
    facetCounts.forEach((key, values) -> {
      Facet facet = facetDescriptionMap.get(key).getFacet(values);
      facets.add(facet);

      System.out.println(key);
      if(facet == null) {
/*
        values.forEach((valKey, bag) -> {
          System.out.println("  " + valKey + ": " + bag.size());
        });*/
      } else {
        facet.getOptions().forEach(System.out::println);
      }
      System.out.println("===");
    });
    /*.forEachRemaining(vertexTraverser -> {
      if(refs.size() < 50) { // FIXME: move responsibility to GET response and introduce limit and offset there
        refs.add(createRef(vertexTraverser.get()));
      }
    })*/;


//    System.out.println(facetCounts);
    // GraphTraversal<Vertex, Vertex> refsClone = searchResult.asAdmin().clone();
    // List<EntityRef> refs = refsClone.map(vertex -> createRef(vertex.get())).toList();

    // List<Facet> facets = createFacets(searchResult.asAdmin().clone());


    return new SearchResult(refs, this, new ArrayList<>());
  }
}
