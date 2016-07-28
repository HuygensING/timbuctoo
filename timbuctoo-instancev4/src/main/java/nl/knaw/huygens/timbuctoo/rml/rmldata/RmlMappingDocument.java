package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.TripleConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RmlMappingDocument {

  private final List<RrTriplesMap> triplesMaps;

  RmlMappingDocument() {
    this.triplesMaps = new ArrayList<>();
  }

  public void execute(TripleConsumer consumer) {
    for (RrTriplesMap triplesMap : triplesMaps) {
      triplesMap.getItems().forEachRemaining(consumer);
    }
  }

  public static Builder rmlMappingDocument() {
    return new Builder();
  }

  public static class Builder implements TripleMapGetter {
    private RmlMappingDocument instance;
    private List<RrTriplesMap.Builder> tripleMapBuilders = new ArrayList<>();
    Map<String, RrTriplesMap> triplesMaps = new HashMap<>();

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
        this.instance.triplesMaps.add(triplesMap);
        triplesMaps.put(triplesMap.getUri().getURI(), triplesMap);
      }
      for (RrTriplesMap.Builder tripleMapBuilder : this.tripleMapBuilders) {
        tripleMapBuilder.fixupTripleMapLinks(this);
      }
      return instance;
    }

    @Override
    public RrTriplesMap getTriplesMap(String uri) {
      return triplesMaps.get(uri);
    }
  }
}
