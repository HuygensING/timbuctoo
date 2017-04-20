package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;

import java.util.ArrayList;
import java.util.List;

public class TypedLiteralDataFetcher implements DataFetcher {
  private final String predicate;
  private final boolean isList;
  private final TripleStore tripleStore;

  public TypedLiteralDataFetcher(String predicate, boolean isList, TripleStore tripleStore) {
    this.predicate = predicate;
    this.isList = isList;
    this.tripleStore = tripleStore;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof BoundSubject) {
      BoundSubject source = environment.getSource();
      try (AutoCloseableIterator<String[]> triples = tripleStore.getTriples(source.getValue(), predicate)) {
        if (isList) {
          List<BoundSubject> result = new ArrayList<>();
          int count = 0;
          while (count++ < 20 && triples.hasNext()) {
            String[] triple = triples.next();
            result.add(new BoundSubject(triple[2], triple[3]));
          }
          return result;
        } else {
          if (triples.hasNext()) {
            String[] triple = triples.next();
            return new BoundSubject(triple[2], triple[3]);
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
