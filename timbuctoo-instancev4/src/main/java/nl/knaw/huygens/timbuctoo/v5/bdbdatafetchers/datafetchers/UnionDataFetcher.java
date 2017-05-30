package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class UnionDataFetcher extends WalkTriplesDataFetcher {
  private final BdbTripleStore tripleStore;

  public UnionDataFetcher(String predicate, boolean isList, BdbTripleStore tripleStore) {
    super(predicate, isList, tripleStore);
    this.tripleStore = tripleStore;
  }

  @Override
  protected BoundSubject makeItem(Quad quad) {
    if (quad.getValuetype().isPresent()) {
      return new BoundSubject(quad.getObject(), quad.getValuetype().get());
    } else {
      try (Stream<Quad> quads = tripleStore.getQuads(quad.getObject(), RDF_TYPE)) {
        final Set<String> types = quads
          .map(Quad::getObject)
          .collect(toSet());
        return new BoundSubject(quad.getObject(), types);
      }
    }
  }
}
