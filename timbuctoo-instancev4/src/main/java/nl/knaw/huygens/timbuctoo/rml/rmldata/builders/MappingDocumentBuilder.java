package nl.knaw.huygens.timbuctoo.rml.rmldata.builders;

import com.google.common.collect.Lists;
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
import java.util.UUID;
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

  /**
   * Sorts the triplesMapBuilders based on the dependencies they have to other triplesMapBuilders
   * In case of unresolved (circular) dependencies, splits off sub-builders.
   * @param triplesMapBuilders the original list of builders
   * @return the sorted and splitted list of builders
   */
  private List<TriplesMapBuilder> sortAndSplitBuilders(List<TriplesMapBuilder> triplesMapBuilders) {

    LinkedList<TriplesMapBuilder> result = topologicalSort(triplesMapBuilders);

    splitOffUnresolvedDependencies(result);

    return result;
  }

  /**
   * Utility class used for marking in topologicalSort
   */
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

  /**
   * Sorts the list of triplesMapBuilders
   * Taken from https://en.wikipedia.org/wiki/Topological_sorting#Depth-first_search
   *
   * <p>
   *   L ← Empty list that will contain the sorted nodes // where L = inputList --> input
   *   while there are unmarked nodes do
   *      select an unmarked node n // where n = current
   *      visit(n) // where visit(n) is sortStep(current, result, input)
   * </p>
   *
   * @param inputList the original list of triplesMapBuilders
   * @return the sorted list of builders
   */
  private LinkedList<TriplesMapBuilder> topologicalSort(List<TriplesMapBuilder> inputList) {
    // L ← Empty list that will contain the sorted nodes
    List<MarkedBuilder> input = inputList.stream().map(MarkedBuilder::new).collect(Collectors.toList());
    LinkedList<TriplesMapBuilder> result = Lists.newLinkedList();

    // while there are unmarked nodes do
    while (input.stream().filter(MarkedBuilder::isUnMarked).iterator().hasNext()) {
      // select an unmarked node n
      MarkedBuilder current = input.stream().filter(MarkedBuilder::isUnMarked).iterator().next();
      // visit(n)
      sortStep(current, result, input);
    }

    return result;
  }

  /**
   * Taken from https://en.wikipedia.org/wiki/Topological_sorting#Depth-first_search
   *
   * <p>
   *  function visit(node n)
   *   if n has a temporary mark then stop (not a DAG) // this check moves to *before* the recursion
   *   if n is not marked (i.e. has not been visited yet) then
   *     mark n temporarily
   *     for each node m with an edge from n to m do
   *       visit(m)
   *     mark n permanently
   *     unmark n temporarily
   *     add n to head of L
   * </p>
   *
   * @param current the next unmarked node from input
   * @param result the sorted result
   * @param input the complete list of input
   */
  private void sortStep(MarkedBuilder current, LinkedList<TriplesMapBuilder> result, List<MarkedBuilder> input) {
    // if n is not marked (i.e. has not been visited yet) then
    if (current.isUnMarked()) {
      // mark n temporarily
      current.mark = MarkedBuilder.MarkType.RUNNING;
      // for each node m with an edge from n to m do
      for (String uriOfDependency : current.triplesMapBuilder.getReferencedTriplesMaps()) {
        Optional<MarkedBuilder> dependentBuilder = input
                .stream()
                .filter(markedBuilder -> markedBuilder.uri.equals(uriOfDependency))
                .findFirst();

        // if m has not a temporary mark then
        if (dependentBuilder.isPresent() && !dependentBuilder.get().isRunning()) {
          // visit(m)
          sortStep(dependentBuilder.get(), result, input);
        }
        // else {
        //   this is not a DAG, there is a circular dependency of node m to node m.
        //   we solve this by stopping the recursion and allowing the sort to place m in the sorted list
        //   as though it did not have this cycle.
        //   the method splitOffUnresolvedDependencies will break the cycle by splitting off the
        //   parts of the mapper which depend on a mapper not yet seen.
        // }
      }
      // mark n permanently
      // unmark n temporarily
      current.mark = MarkedBuilder.MarkType.DONE;
      // add n to head of L
      result.add(current.triplesMapBuilder);
    }
  }

  /**
   * Splits off the parts of all triplesMapBuilders that depend on another builder that comes after it in the sorted
   * list:
   *
   * <p>
   *   Given:  x depends on [y]
   *   And:    y depends on [x]
   *   And:    sortedList = [x, y]
   *   And:    splitOffs = []
   *
   *   Create: x' depends on y
   *   Make:   x not depend on y
   *   Add:    splitOffs.add(x')
   *
   *   After:  sortedList.concat(splitOffs) --> [x, y].concat([x']) --> [x, y, x']
   * </p>
   * @param sortedList the sorted list of triples map builders
   */
  private void splitOffUnresolvedDependencies(LinkedList<TriplesMapBuilder> sortedList) {
    Set<TriplesMapBuilder> done = new HashSet<>();
    List<TriplesMapBuilder> splitOffs = new LinkedList<>();

    for (TriplesMapBuilder current : sortedList) {
      // Loop through all the triplesMapBuilders the current builder depends on
      for (String uriOfReferencedTriplesMap : current.getReferencedTriplesMaps()) {

        // Contains all the URIs of triplesMapBuilders which can safely be depended upon by the current builder
        final List<String> resolvedDependencies = done
          .stream()
          .map(TriplesMapBuilder::getUri)
          .collect(Collectors.toList());

        // If the current builder depends on a builder that comes after it in the sorted list...
        if (!resolvedDependencies.contains(uriOfReferencedTriplesMap)) {
          // Remove all the predicate-object-map-builders that refer to the unresolved dependency
          // and store them in a list (refs)
          final List<PredicateObjectMapBuilder> refs = current.withoutPredicatesReferencing(uriOfReferencedTriplesMap);

          // Create a new triplesMapBuilder with the PredicateObjectMapBuilders referencing the unresolved dependency
          final String newTriplesMapUri = String.format("%s/split/%s", current.getUri(), UUID.randomUUID());
          final TriplesMapBuilder triplesMapBuilderWithReferences =
            new TriplesMapBuilder(newTriplesMapUri)
              .withLogicalSource(current.getLogicalSource())
              .withSubjectMapBuilder(current.getSubjectMapBuilder())
              .withPredicateObjectMapBuilders(refs);

          // add this new builder to the list of split-offs
          splitOffs.add(triplesMapBuilderWithReferences);
        }
      }
      // Now we are done with current
      done.add(current);
    }
    // Add all the splitOffs at the end of the sortedList, in this stage it is impossible to have any triplesMapBuilders
    // with unresolved dependencies on other builders.
    sortedList.addAll(splitOffs);
  }

  public RmlMappingDocument build(Function<RdfResource, Optional<DataSource>> dataSourceFactory) {

    final List<RrTriplesMap> triplesMaps = sortAndSplitBuilders(this.tripleMapBuilders).stream()
      // Build the tripleMapBuilders with lambda to resolve otherMap they are dependent on
      .map(tripleMapBuilder -> tripleMapBuilder.build(dataSourceFactory, this::getRrTriplesMap, errors::add))
      .filter(x -> x != null)
      // First collect all the builders, so requestedTripleMaps is filled via getRrTriplesMap lambda
      .collect(Collectors.toList())
      .stream()
      // Resolve uri's of requested triple maps to actual RrTripleMap using PromisedTriplesMap.setTriplesMap
      .map(current -> {
        if (requestedTripleMaps.containsKey(current.getUri())) {
          requestedTripleMaps.get(current.getUri()).forEach(promise -> promise.setTriplesMap(current));
        }
        return current;
      })
      // Recollect to final list
      .collect(Collectors.toList());

    return new RmlMappingDocument(triplesMaps, errors);
  }

  private PromisedTriplesMap getRrTriplesMap(String requesterUri, String requestedUri) {
    PromisedTriplesMap promisedTriplesMap = new PromisedTriplesMap();

    requestedTripleMaps
      .computeIfAbsent(requestedUri, key -> new ArrayList<>())
      .add(promisedTriplesMap);

    return promisedTriplesMap;
  }
}
