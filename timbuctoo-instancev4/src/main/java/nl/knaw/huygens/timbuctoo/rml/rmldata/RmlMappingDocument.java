package nl.knaw.huygens.timbuctoo.rml.rmldata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RmlMappingDocument implements Iterable<RrTriplesMap> {

  private final List<RrTriplesMap> triplesMaps;

  RmlMappingDocument() {
    this.triplesMaps = new ArrayList<>();
  }

  @Override
  public Iterator<RrTriplesMap> iterator() {
    return triplesMaps.iterator();
  }

  public static Builder rmlMappingDocument() {
    return new Builder();
  }

  public static class Builder {
    private RmlMappingDocument instance;
    private RrTriplesMap.Builder tripleMapBuilder;

    public Builder() {
      this.instance = new RmlMappingDocument();
    }

    public Builder withTripleMap(RrTriplesMap.Builder subBuilder) {
      this.tripleMapBuilder = subBuilder;
      return this;
    }

    public RrTriplesMap.Builder withTripleMap() {
      final RrTriplesMap.Builder subBuilder = new RrTriplesMap.Builder();
      this.tripleMapBuilder = subBuilder;
      return subBuilder;
    }

    public RmlMappingDocument build() {
      this.instance.triplesMaps.add(tripleMapBuilder.build());
      return instance;
    }
  }
}
