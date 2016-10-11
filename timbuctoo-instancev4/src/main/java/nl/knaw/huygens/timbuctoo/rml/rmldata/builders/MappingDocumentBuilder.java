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
  private Map<String, List<PromisedTriplesMapBuilder>> requestedTripleMapBuilders = new HashMap<>();
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

  /*
  SortedList ← Empty list that will contain the sorted nodes
  while there are unmarked nodes do
      select a node n that os not yet in list SortedList
      sortStep(n)
  function sortStep(node n)
      if n has a "running" mark (which means that it was visited during this recursion) then stop (not a DAG)
      if n is not done then
          mark n "running"
          for each node m with an edge from n to m do
              sortStep(m)
          unmark n "running"
          add n to head of SortedList
   */
  //Algorithm from https://en.wikipedia.org/wiki/Topological_sorting#Depth-first_search
  //adapted to be able to handle cyclic graphs.
  //A cycle is broken by inverting the edge
  private List<TriplesMapBuilder> topologicalSort(Map<String, TriplesMapBuilder> input, List<String> errors) {

    Set<TriplesMapBuilder> done = new HashSet<>();
    LinkedList<TriplesMapBuilder> result = new LinkedList<>();

    for (TriplesMapBuilder rrTriplesMap : input.values()) {
      sortStep(rrTriplesMap, new HashSet<>(), done, result, input);
    }

    for (String uri : requestedTripleMapBuilders.keySet()) {
      errors.add("Triple map with URI " + uri + " is referenced as the parentTriplesMap of a referencingObjectMap, " +
              "but never defined");
    }
    return result;
  }

  private void sortStep(TriplesMapBuilder current, Set<TriplesMapBuilder> running, Set<TriplesMapBuilder> done,
                        LinkedList<TriplesMapBuilder> result, Map<String, TriplesMapBuilder> input) {
    //   if n is not marked (i.e. has not been visited yet) then
    if (!done.contains(current)) {
      //      mark n temporarily
      running.add(current);
      //      for each node m with an edge from n to m do
      if (requestedTripleMapBuilders.containsKey(current.getUri())) {
        for (PromisedTriplesMapBuilder promise : requestedTripleMapBuilders.get(current.getUri())) {
          TriplesMapBuilder requester = input.get(promise.getRequesterUri());
          if (running.contains(requester)) {
            // if n has a temporary mark then invert the edge to make it an acyclic graph again
            /*
              requestedTripleMaps
              .get(requester.getUri()) //must exist to be able to get here
              .add(new PromisedTriplesMap(current.getUri(), true));*/
          } else {
            //  if not: then recurse
            sortStep(requester, running, done, result, input);
          }
          //really create the edges
        }
        requestedTripleMapBuilders.remove(current.getUri());
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
    final Map<String, TriplesMapBuilder> builderMaps = this.tripleMapBuilders
      .stream()
      .map(tripleMapBuilder -> {
        tripleMapBuilder
                .getReferencedTriplesMaps()
                .forEach(requestedUri -> putTriplesMapBuilder(tripleMapBuilder.getUri(), requestedUri));
        return tripleMapBuilder;
      })
      .collect(Collectors.toMap(TriplesMapBuilder::getUri, x -> x));

    final List<RrTriplesMap> triplesMaps = topologicalSort(builderMaps, errors)
      .stream()
      .map(tripleMapBuilder -> tripleMapBuilder.build(dataSourceFactory, this::getRrTriplesMap, errors::add))
      .filter(x -> x != null)
      .collect(Collectors.toList());

    // FIXME: promise + flip can be factored out
    for (RrTriplesMap current : triplesMaps) {
      if (requestedTripleMaps.containsKey(current.getUri())) {
        for (PromisedTriplesMap promise : requestedTripleMaps.get(current.getUri())) {
          promise.setTriplesMap(current, false);
        }
      }
    }

    return new RmlMappingDocument(triplesMaps, errors);
  }

  private void putTriplesMapBuilder(String requesterUri, String requestedUri) {
    Optional<TriplesMapBuilder> found = this.tripleMapBuilders
            .stream().filter(builder -> builder.getUri().equals(requestedUri)).findFirst();

    if (found.isPresent()) {
      PromisedTriplesMapBuilder promisedTriplesMapBuilder =
              new PromisedTriplesMapBuilder(requesterUri, found.get());

      requestedTripleMapBuilders
              .computeIfAbsent(found.get().getUri(), key -> new ArrayList<>())
              .add(promisedTriplesMapBuilder);
    }
  }

  private PromisedTriplesMap getRrTriplesMap(String requesterUri, String requestedUri) {
    PromisedTriplesMap promisedTriplesMap = new PromisedTriplesMap(requesterUri, false);

    requestedTripleMaps
      .computeIfAbsent(requestedUri, key -> new ArrayList<>())
      .add(promisedTriplesMap);

    return promisedTriplesMap;
  }
}
