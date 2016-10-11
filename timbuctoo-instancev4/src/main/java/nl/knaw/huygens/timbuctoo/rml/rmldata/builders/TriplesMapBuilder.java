package nl.knaw.huygens.timbuctoo.rml.rmldata.builders;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

  List<String> getReferencedTriplesMaps() {
    return predicateObjectMapBuilders.stream()
            .map(PredicateObjectMapBuilder::getReferencedMap)
            .filter(x -> x != null)
            .collect(Collectors.toList());
  }

  RrTriplesMap build(Function<RdfResource, Optional<DataSource>> dataSourceFactory,
                     BiFunction<String, String, PromisedTriplesMap> getTriplesMap, Consumer<String> errorLogger) {


    Optional<DataSource> dataSource = dataSourceFactory.apply(logicalSource);
    if (dataSource.isPresent()) {
      RrTriplesMap instance = new RrTriplesMap(
        subjectMapBuilder.build(predicateObjectMapBuilders::add),
        dataSource.get(),
        uri
      );

      for (PredicateObjectMapBuilder builder : this.predicateObjectMapBuilders) {
        builder.build(requesteduri -> getTriplesMap.apply(this.uri, requesteduri), instance);
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
}
