package nl.knaw.huygens.timbuctoo.rml.rmldata.builders;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


  private static class MarkedBuilder {
    enum MarkType { UNMARKED, RUNNING, DONE }

    TriplesMapBuilder triplesMapBuilder;
    String uri;
    MarkType mark = MarkType.UNMARKED;

    MarkedBuilder(TriplesMapBuilder triplesMapBuilder) {
      this.triplesMapBuilder = triplesMapBuilder;
      this.uri = triplesMapBuilder.getUri();
    }

    boolean isUnMarked() {
      return mark == MarkType.UNMARKED;
    }

    boolean isRunning() {
      return mark == MarkType.RUNNING;
    }
  }

  private List<TriplesMapBuilder> topologicalSortAndSplit(List<TriplesMapBuilder> inputList) {

    List<MarkedBuilder> input = inputList.stream().map(MarkedBuilder::new).collect(Collectors.toList());
    LinkedList<TriplesMapBuilder> result = Lists.newLinkedList();


    while (input.stream().filter(MarkedBuilder::isUnMarked).iterator().hasNext()) {
      MarkedBuilder current = input.stream().filter(MarkedBuilder::isUnMarked).iterator().next();
      sortStep(current, Lists.newArrayList(current.uri), result, input);
    }

    Set<TriplesMapBuilder> done = new HashSet<>();
    List<TriplesMapBuilder> referenced = new LinkedList<>();

    for (TriplesMapBuilder current : result) {
      for (String uriOfReferencedTriplesMap : current.getReferencedTriplesMaps()) {
        if (!done.stream().map(d -> d.getUri()).collect(Collectors.toList()).contains(uriOfReferencedTriplesMap)) {
          final List<PredicateObjectMapBuilder> refs = current.withoutPredicatesReferencing(uriOfReferencedTriplesMap);

          // Create a new triplesMapBuilder with the PredicateObjectMapBuilders referencing the requester
          final String newTriplesMapUri = String.format("%s/split/%s", current.getUri(), UUID.randomUUID());
          final TriplesMapBuilder triplesMapBuilderWithReferences =
                  new TriplesMapBuilder(newTriplesMapUri)
                          .withLogicalSource(current.getLogicalSource())
                          .withSubjectMapBuilder(current.getSubjectMapBuilder())
                          .withPredicateObjectMapBuilders(refs);

          referenced.add(triplesMapBuilderWithReferences);
        }
      }


      done.add(current);
    }
    result.addAll(referenced);
    return result;
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
  private void sortStep(MarkedBuilder current, List<String> pathToCurrent,
                        LinkedList<TriplesMapBuilder> result,
                        List<MarkedBuilder> input) {
    if (current.isUnMarked()) {
      current.mark = MarkedBuilder.MarkType.RUNNING;
      for (String uriOfDependency : current.triplesMapBuilder.getReferencedTriplesMaps()) {
        MarkedBuilder dependentBuilder = findBuilderForUri(input, uriOfDependency);
        if (dependentBuilder != null) {

          List<String> pathToDepenentBuilder = Stream
                  .concat(pathToCurrent.stream(), Lists.newArrayList(dependentBuilder.uri).stream())
                  .collect(Collectors.toList());

          if (dependentBuilder.isRunning()) {
            // fix this circular dependency later,
            // sort everything we can sort here.
          } else {
            sortStep(dependentBuilder, pathToDepenentBuilder, result, input);
          }
        } else {
          // throw
        }
      }

      current.mark = MarkedBuilder.MarkType.DONE;
      result.add(current.triplesMapBuilder);
    }
  }

  private MarkedBuilder findBuilderForUri(List<MarkedBuilder> input, String uri) {
    Iterator<MarkedBuilder> markedBuilderIterator = input
            .stream()
            .filter(markedBuilder -> markedBuilder.uri.equals(uri))
            .iterator();
    return markedBuilderIterator.hasNext() ?
            markedBuilderIterator.next() : null;
  }

  public RmlMappingDocument build(Function<RdfResource, Optional<DataSource>> dataSourceFactory) {

    final List<RrTriplesMap> triplesMaps = topologicalSortAndSplit(this.tripleMapBuilders)
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

  private PromisedTriplesMap getRrTriplesMap(String requesterUri, String requestedUri) {
    PromisedTriplesMap promisedTriplesMap = new PromisedTriplesMap(requesterUri, false);

    requestedTripleMaps
      .computeIfAbsent(requestedUri, key -> new ArrayList<>())
      .add(promisedTriplesMap);

    return promisedTriplesMap;
  }
}
