package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;

import java.util.stream.Stream;

public abstract class RelatedDataFetcher implements DataFetcher {
  protected final String predicate;
  protected final boolean isList;
  protected final TripleStore tripleStore;

  public RelatedDataFetcher(String predicate, TripleStore tripleStore, boolean isList) {
    this.predicate = predicate;
    this.tripleStore = tripleStore;
    this.isList = isList;
  }

  protected abstract BoundSubject makeItem(Quad quad);

  @Override
  public Object get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof BoundSubject) {
      String after = environment.getArgument("after");
      String before = environment.getArgument("before");
      Integer last = environment.getArgument("last");
      Integer first = environment.getArgument("first");
      BoundSubject source = environment.getSource();
      if (isList) {
        return getList(source, first, after, last, before);
      } else {
        return getItem(source);
      }
    } else {
      throw new IllegalStateException("Source is not a BoundSubject");
    }

  }

  BoundSubject getItem(BoundSubject source) {
    return tripleStore.getFirst(source.getValue(), predicate)
      .map(this::makeItem)
      .orElse(null);
  }

  PaginatedList getList(BoundSubject source, Integer first, String after, Integer last, String before) {
    return new PaginatedList(
      this::makeItem,
      (ascending, cursor) -> getQuadStream(tripleStore, source, predicate, ascending, cursor),
      after,
      before,
      first,
      last
    );
  }

  private Stream<Tuple<String, Quad>> getQuadStream(TripleStore tripleStore, BoundSubject source, String predicate,
                                                    boolean ascending, String cursor) {
    final Stream<Tuple<String, Quad>> items;
    if (cursor == null) {
      items = tripleStore.getQuadsWithoutGraph(source.getValue(), predicate, ascending);
    } else {
      items = tripleStore.getQuadsWithoutGraph(cursor, source.getValue(), predicate, ascending);
    }
    return items;
  }
}
