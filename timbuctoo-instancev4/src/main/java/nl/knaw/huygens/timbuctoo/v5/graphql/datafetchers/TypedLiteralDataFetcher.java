package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
      try (Stream<Quad> quads = tripleStore.getQuads(source.getValue(), predicate)) {
        Stream<BoundSubject> subjects = quads
          .map(quad -> new BoundSubject(quad.getObject(), quad.getValuetype().orElse(null)));
        if (isList) {
          return subjects
            .limit(20)
            .collect(toList());
        } else {
          return subjects
            .findFirst()
            .orElse(null);
        }
      }
    } else {
      throw new IllegalStateException("Source is not a BoundSubject");
    }
  }
}
