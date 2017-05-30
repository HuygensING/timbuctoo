package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class WalkTriplesDataFetcher implements DataFetcher {
  private final String predicate;
  private final boolean isList;
  private final BdbTripleStore tripleStore;

  public WalkTriplesDataFetcher(String predicate, boolean isList, BdbTripleStore tripleStore) {
    this.predicate = predicate;
    this.isList = isList;
    this.tripleStore = tripleStore;
  }

  protected abstract BoundSubject makeItem(Quad quad);

  @Override
  public Object get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof BoundSubject) {
      BoundSubject source = environment.getSource();
      try (Stream<Quad> quads = tripleStore.getQuads(source.getValue(), predicate)) {
        if (isList) {
          return quads
            .map(this::makeItem)
            .limit(20)
            .collect(Collectors.toList());
        } else {
          return quads.findFirst()
            .map(this::makeItem)
            .orElse(null);
        }
      }
    } else {
      throw new IllegalStateException("Source is not a BoundSubject");
    }
  }

}
