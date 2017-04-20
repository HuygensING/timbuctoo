package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;

import java.util.ArrayList;
import java.util.List;

public class RelationDataFetcher implements DataFetcher {
  private final String predicate;
  private final boolean isList;
  private final TripleStore tripleStore;

  public RelationDataFetcher(String predicate, boolean isList, TripleStore tripleStore) {
    this.predicate = predicate;
    this.isList = isList;
    this.tripleStore = tripleStore;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    List<BoundSubject> result = new ArrayList<>();
    if (environment.getSource() instanceof BoundSubject) {
      BoundSubject source = environment.getSource();
      try (AutoCloseableIterator<String[]> triples = tripleStore.getTriples(source.getValue(), predicate)) {
        if (isList) {
          int count = 0;
          while (count++ < 20 && triples.hasNext()) {
            result.add(new BoundSubject(triples.next()[2]));
          }
          return result;
        } else {
          if (triples.hasNext()) {
            return new BoundSubject(triples.next()[2]);
          } else {
            return null;
          }
        }
      }
    } else {
      throw new IllegalStateException("Source is not a BoundSubject");
    }
  }
}
