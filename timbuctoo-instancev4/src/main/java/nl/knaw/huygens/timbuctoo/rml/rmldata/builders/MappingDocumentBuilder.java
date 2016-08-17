package nl.knaw.huygens.timbuctoo.rml.rmldata.builders;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MappingDocumentBuilder {
  private List<TriplesMapBuilder> tripleMapBuilders = new ArrayList<>();
  private Map<String, PromisedTriplesMap> requestedTripleMaps = new HashMap<>();
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

  public RmlMappingDocument build(Function<RdfResource, DataSource> dataSourceFactory) {
    List<RrTriplesMap> triplesMaps =
      this.tripleMapBuilders.stream()
                            .map(tripleMapBuilder -> tripleMapBuilder.build(dataSourceFactory, this::getRrTriplesMap))
                            .collect(Collectors.toList());
    Set<RrTriplesMap> seenTriplesMaps = new HashSet<>();
    triplesMaps.forEach(map -> {
      if (requestedTripleMaps.containsKey(map.getUri())) {
        requestedTripleMaps.get(map.getUri()).setTriplesMap(map, seenTriplesMaps);
        requestedTripleMaps.remove(map.getUri());
      }
      seenTriplesMaps.add(map);
    });

    for (String uri : requestedTripleMaps.keySet()) {
      errors.add("Triple map with URI " + uri + " is referenced as the parentTriplesMap of a referencingObjectMap, " +
        "but never defined");
    }

    return new RmlMappingDocument(triplesMaps, errors);
  }

  private PromisedTriplesMap getRrTriplesMap(String uri) {
    return requestedTripleMaps.computeIfAbsent(uri, key -> new PromisedTriplesMap());
  }
}
