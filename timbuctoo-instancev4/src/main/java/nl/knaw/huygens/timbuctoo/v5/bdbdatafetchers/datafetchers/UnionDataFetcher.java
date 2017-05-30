package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class UnionDataFetcher implements DataFetcher {
  private final String predicate;
  private final boolean isList;
  private final BdbTripleStore tripleStore;

  public UnionDataFetcher(String predicate, boolean isList, BdbTripleStore store) {
    this.predicate = predicate;
    this.isList = isList;
    this.tripleStore = store;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof BoundSubject) {

      BoundSubject source = environment.getSource();
      try (Stream<Quad> quads = tripleStore.getQuads(source.getValue(), predicate)) {
        if (isList) {
          return quads
            .map(quad -> makeItem(quad))
            .filter(Objects::nonNull)
            .limit(20)
            .collect(toList());
        } else {
          return quads.findFirst()
            .map(quad -> makeItem(quad))
            .orElse(null);
        }
      }
    } else {
      throw new IllegalStateException("Source is not a BoundSubject");
    }
  }

  private BoundSubject makeItem(Quad quad) {
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
