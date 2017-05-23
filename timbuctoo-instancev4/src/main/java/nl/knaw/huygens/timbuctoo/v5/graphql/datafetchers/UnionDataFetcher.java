package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class UnionDataFetcher extends RelatedDataFetcher {

  public UnionDataFetcher(String predicate, boolean isList, TripleStore store) {
    super(predicate, store, isList);
  }

  @Override
  protected BoundSubject makeItem(Quad quad) {
    if (quad.getValuetype().isPresent()) {
      return new BoundSubject(quad.getObject(), quad.getValuetype().get());
    } else {
      return new BoundSubject(quad.getObject(), getTypes(quad.getObject()));
    }
  }

  private Set<String> getTypes(String uri) {
    try (Stream<Quad> quads = tripleStore.getQuads(uri, RDF_TYPE)) {
      return quads
        .map(Quad::getObject)
        .collect(toSet());
    }
  }


}
