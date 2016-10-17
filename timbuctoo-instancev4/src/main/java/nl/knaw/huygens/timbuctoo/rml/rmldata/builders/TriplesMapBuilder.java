package nl.knaw.huygens.timbuctoo.rml.rmldata.builders;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


public class TriplesMapBuilder {
  private RdfResource logicalSource;
  private SubjectMapBuilder subjectMapBuilder;
  private List<PredicateObjectMapBuilder> predicateObjectMapBuilders = new ArrayList<>();
  private final String uri;

  TriplesMapBuilder(String uri) {
    this.uri = uri;
  }

  public TriplesMapBuilder withLogicalSource(RdfResource logicalSource) {
    this.logicalSource = logicalSource;
    return this;
  }

  public TriplesMapBuilder withSubjectMap(Consumer<SubjectMapBuilder> subBuilder) {
    subBuilder.accept(withSubjectMap());
    return this;
  }

  public SubjectMapBuilder withSubjectMap() {
    this.subjectMapBuilder = new SubjectMapBuilder();
    return this.subjectMapBuilder;
  }


  public TriplesMapBuilder withPredicateObjectMap(Consumer<PredicateObjectMapBuilder> subBuilder) {
    subBuilder.accept(withPredicateObjectMap());
    return this;
  }

  public PredicateObjectMapBuilder withPredicateObjectMap() {
    final PredicateObjectMapBuilder
      subBuilder = new PredicateObjectMapBuilder();
    this.predicateObjectMapBuilders.add(subBuilder);
    return subBuilder;
  }

  public Set<String> getReferencedTriplesMaps() {
    return predicateObjectMapBuilders.stream()
      .map(PredicateObjectMapBuilder::getReferencedMap)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toSet());
  }

  RrTriplesMap build(Function<RdfResource, Optional<DataSource>> dataSourceFactory,
                     BiFunction<String, String, PromisedTriplesMap> getTriplesMap, Consumer<String> errorLogger) {


    Optional<DataSource> dataSource = dataSourceFactory.apply(logicalSource);
    if (dataSource.isPresent()) {

      RrTriplesMap instance = new RrTriplesMap(
        subjectMapBuilder.build(x -> predicateObjectMapBuilders.add(x)),
        dataSource.get(),
        uri
      );

      for (PredicateObjectMapBuilder builder : this.predicateObjectMapBuilders) {

        try {
          builder.build(requesteduri -> getTriplesMap.apply(this.uri, requesteduri), instance);
        } catch (IOException e) {
          errorLogger.accept(e.getMessage());
        }
      }
      return instance;
    } else {
      errorLogger.accept("No datasource could be constructed for map " + uri);
      return null;
    }
  }

  public String getUri() {
    return uri;
  }

  /**
   * Removes the predObjMap-builders which depend on predObjMap-builders not in the resolved list (from current
   * triplesMap builder) and returns them
   * @param resolved the resolved dependencies (which come before current)
   * @return all the unresolvedDependencies to other triplesMapBuilder for the current triplesMapBuilder
   */
  Optional<TriplesMapBuilder> splitOffUnresolvedDependencies(Set<String> resolved) {

    Set<String> referencedTriplesMaps = getReferencedTriplesMaps();
    Sets.SetView<String> unresolvedDependencies = Sets.difference(
      referencedTriplesMaps,
      Sets.intersection(
        referencedTriplesMaps,
        resolved
      )
    );

    final List<PredicateObjectMapBuilder> refsToUnresolvedDependencies = new ArrayList<>();
    for (String uriOfReferencedTriplesMap : unresolvedDependencies) {
      for (int i = predicateObjectMapBuilders.size() - 1; i >= 0; i--) {
        PredicateObjectMapBuilder referencingObjMap = predicateObjectMapBuilders.get(i);
        if (referencingObjMap.getReferencedMap().isPresent()) {
          if (uriOfReferencedTriplesMap.equals(referencingObjMap.getReferencedMap().get())) {
            refsToUnresolvedDependencies.add(referencingObjMap);
            predicateObjectMapBuilders.remove(referencingObjMap);
          }
        }
      }
    }

    if (refsToUnresolvedDependencies.size() > 0) {
      // Create a new triplesMapBuilder with the PredicateObjectMapBuilders referencing the unresolved dependency
      return Optional.of(createTriplesMapBuilderFrom(refsToUnresolvedDependencies));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Creates a new triplesMapBuilder which has the same logical source + subject map as current and the
   * PredicateObjectMapBuilders in builders
   * @param builders the PredicateObjectMapBuilders which depend on unresolved pom builders
   * @return the new triplesMapBuilder
   */
  private TriplesMapBuilder createTriplesMapBuilderFrom(List<PredicateObjectMapBuilder> builders) {
    TriplesMapBuilder result = new TriplesMapBuilder(this.uri + "/split/" + UUID.randomUUID());
    result.logicalSource = this.logicalSource;
    result.subjectMapBuilder = this.subjectMapBuilder;
    result.predicateObjectMapBuilders = builders;
    return result;
  }

}
