package nl.knaw.huygens.timbuctoo.rml.rmldata.builders;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MappingDocumentBuilder {
  private List<TriplesMapBuilder> tripleMapBuilders = new ArrayList<>();
  private Map<String, List<PromisedTriplesMap>> requestedTripleMaps = new HashMap<>();
  private List<String> errors = new ArrayList<>();

  public MappingDocumentBuilder(){
  }

  public MappingDocumentBuilder withTripleMap(String uri, Consumer<TriplesMapBuilder> subBuilder) {
    subBuilder.accept(withTripleMap(uri));
    return this;
  }

  public TriplesMapBuilder withTripleMap(String uri) {
    final TriplesMapBuilder subBuilder = new TriplesMapBuilder(uri);
    this.tripleMapBuilders.add(subBuilder);
    return subBuilder;
  }

  //Algorithm from https://en.wikipedia.org/wiki/Topological_sorting#Depth-first_search
  //adapted to be able to handle cyclic graphs.
  //A cycle is broken by inverting the edge
  private List<RrTriplesMap> topologicalSort(Map<String, RrTriplesMap> input, List<String> errors) {
    Set<RrTriplesMap> done = new HashSet<>();
    LinkedList<RrTriplesMap> result = new LinkedList<>();

    for (RrTriplesMap rrTriplesMap : input.values()) {
      sortStep(rrTriplesMap, new HashSet<>(), done, result, input);
    }

    for (String uri : requestedTripleMaps.keySet()) {
      errors.add("Triple map with URI " + uri + " is referenced as the parentTriplesMap of a referencingObjectMap, " +
        "but never defined");
    }
    return result;
  }

  private void sortStep(RrTriplesMap current, Set<RrTriplesMap> running, Set<RrTriplesMap> done,
                        LinkedList<RrTriplesMap> result, Map<String, RrTriplesMap> input) {
    //   if n is not marked (i.e. has not been visited yet) then
    if (!done.contains(current)) {
      //      mark n temporarily
      running.add(current);
      //      for each node m with an edge from n to m do
      if (requestedTripleMaps.containsKey(current.getUri())) {
        for (PromisedTriplesMap promise : requestedTripleMaps.get(current.getUri())) {
          RrTriplesMap requester = input.get(promise.getRequesterUri());
          if (running.contains(requester)) {
            //  if n has a temporary mark then invert the edge to make it an acyclic graph again
/*            requestedTripleMaps
              .get(requester.getUri()) //must exist to be able to get here
              .add(new PromisedTriplesMap(current.getUri(), true));*/
          } else {
            //  if not: then recurse
            sortStep(requester, running, done, result, input);
          }
          //really create the edges
          promise.setTriplesMap(current, promise.isFlipped());
        }
        requestedTripleMaps.remove(current.getUri());
      }
      //      mark n permanently
      done.add(current);
      //      unmark n temporarily
      running.remove(current);
      //      add n to head of L
      result.addFirst(current);
    }
  }

  public RmlMappingDocument build(Function<RdfResource, Optional<DataSource>> dataSourceFactory) {
/*
    Map<String, RrTriplesMap> triplesMaps =
      this.tripleMapBuilders.stream()
                            .map(tripleMapBuilder -> tripleMapBuilder.build(dataSourceFactory, this::getRrTriplesMap,
                              errors::add))
                            .filter(x -> x != null)
                            .collect(Collectors.toMap(RrTriplesMap::getUri, x -> x));

    return new RmlMappingDocument(topologicalSort(triplesMaps, errors), errors);

*/

    List<RrTriplesMap> triplesMaps =
      this.tripleMapBuilders.stream()
                            .map(tripleMapBuilder -> tripleMapBuilder.build(dataSourceFactory, this::getRrTriplesMap,
                              errors::add))
                            .filter(x -> x != null)
                            .collect(Collectors.toList());

    for(RrTriplesMap current : triplesMaps) {
      if (requestedTripleMaps.containsKey(current.getUri())) {
        for (PromisedTriplesMap promise : requestedTripleMaps.get(current.getUri())) {
          promise.setTriplesMap(current, false);
        }
      }
    }

    return new RmlMappingDocument(triplesMaps, errors);
  }

  private PromisedTriplesMap getRrTriplesMap(String requesterUri, String requestedUri) {
    PromisedTriplesMap promisedTriplesMap = new PromisedTriplesMap(requesterUri, false);

    requestedTripleMaps
      .computeIfAbsent(requestedUri, key -> new ArrayList<>())
      .add(promisedTriplesMap);

    return promisedTriplesMap;
  }
}
