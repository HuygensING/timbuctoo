package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;

import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if (isList) {
          return quads
            .map(triple ->
              triple.getValuetype()
                .map(valueType -> new BoundSubject(triple.getObject(), valueType))
                .orElseGet(() -> new BoundSubject(triple.getObject()))
            )
            .limit(20)
            .collect(Collectors.toList());
        } else {
          return quads.findFirst()
            .map(triple ->
              triple.getValuetype()
                .map(valueType -> new BoundSubject(triple.getObject(), valueType))
                .orElseGet(() -> new BoundSubject(triple.getObject()))
            )
            .orElse(null);
        }
      }
    } else {
      throw new IllegalStateException("Source is not a BoundSubject");
    }
  }
}
