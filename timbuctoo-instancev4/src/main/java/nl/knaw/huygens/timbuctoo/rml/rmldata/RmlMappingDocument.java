package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class RmlMappingDocument {

  private final List<RrTriplesMap> triplesMaps;

  RmlMappingDocument() {
    this.triplesMaps = new ArrayList<>();
  }

  public Stream<Triple> execute() {
    return triplesMaps.stream().flatMap(RrTriplesMap::getItems);
  }

  public static Builder rmlMappingDocument() {
    return new Builder();
  }

  public static class Builder implements Function<String, Tuple<Integer, RrTriplesMap>> {
    private RmlMappingDocument instance;
    private List<RrTriplesMap.Builder> tripleMapBuilders = new ArrayList<>();
    private Map<String, Tuple<Integer, RrTriplesMap>> triplesMaps = new HashMap<>();

    public Builder() {
      this.instance = new RmlMappingDocument();
    }

    public Builder withTripleMap(RrTriplesMap.Builder subBuilder) {
      this.tripleMapBuilders.add(subBuilder);
      return this;
    }

    public RrTriplesMap.Builder withTripleMap() {
      final RrTriplesMap.Builder subBuilder = new RrTriplesMap.Builder();
      this.tripleMapBuilders.add(subBuilder);
      return subBuilder;
    }

    public RmlMappingDocument build(Function<RrLogicalSource, DataSource> dataSourceFactory) {
      for (RrTriplesMap.Builder tripleMapBuilder : this.tripleMapBuilders) {
        final RrTriplesMap triplesMap = tripleMapBuilder.build(dataSourceFactory);
        triplesMaps.put(triplesMap.getUri().getURI(), tuple(this.instance.triplesMaps.size(), triplesMap));
        this.instance.triplesMaps.add(triplesMap);
      }
      for (int i = 0; i < tripleMapBuilders.size(); i++) {
        tripleMapBuilders.get(i).fixupTripleMapLinks(this, i);
      }
      return instance;
    }

    @Override
    public Tuple<Integer, RrTriplesMap> apply(String uri) {
      return triplesMaps.get(uri);
    }
  }
}
