package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelationDataFetcher implements DataFetcher {
  private final String predicate;
  private final boolean isList;
  private final BdbTripleStore tripleStore;

  public RelationDataFetcher(String predicate, boolean isList, BdbTripleStore tripleStore) {
    this.predicate = predicate;
    this.isList = isList;
    this.tripleStore = tripleStore;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof BoundSubject) {
      BoundSubject source = environment.getSource();
      try (Stream<Quad> quads = tripleStore.getQuads(source.getValue(), predicate)) {
        if (isList) {
          return quads
            .map(triple -> new BoundSubject(triple.getObject()))
            .limit(20)
            .collect(Collectors.toList());
        } else {
          return quads.findFirst()
            .map(triple -> new BoundSubject(triple.getObject()))
            .orElse(null);
        }
      }
    } else {
      throw new IllegalStateException("Source is not a BoundSubject");
    }
  }
}
