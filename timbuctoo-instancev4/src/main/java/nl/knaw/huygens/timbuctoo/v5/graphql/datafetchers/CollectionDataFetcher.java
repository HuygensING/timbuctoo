package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.MarkedSubject;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;

import java.util.stream.Stream;

public class CollectionDataFetcher implements DataFetcher {
  private final String collectionName;
  private final CollectionIndex collectionIndex;

  public CollectionDataFetcher(String collectionName, CollectionIndex collectionIndex) {

    this.collectionName = collectionName;
    this.collectionIndex = collectionIndex;
  }

  private BoundSubject makeItem(String input) {
    return new BoundSubject(input);
  }

  @Override
  public PaginatedList get(DataFetchingEnvironment environment) {
    String after = environment.getArgument("after");
    String before = environment.getArgument("before");
    Integer first = environment.getArgument("first");
    Integer last = environment.getArgument("last");
    return getList(after, before, first, last);
  }

  public PaginatedList getList(String after, String before, Integer first, Integer last) {
    return new PaginatedList(
      this::makeItem,
      (ascending, cursor) -> {
        Stream<MarkedSubject> result;
        if (cursor == null) {
          result = collectionIndex.getSubjects(collectionName, ascending);
        } else {
          result = collectionIndex.getSubjects(collectionName, ascending, cursor);
        }
        return result.map(s -> Tuple.tuple(s.getMarker(), s.getSubject()));
      },
      after,
      before,
      first,
      last
    );
  }
}
