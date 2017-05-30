package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionDataFetcher implements DataFetcher {
  private final String collectionName;
  private final CollectionIndex collectionIndex;
  private static final int MAX_ITEMS = 20;

  public CollectionDataFetcher(String collectionName, CollectionIndex collectionIndex) {

    this.collectionName = collectionName;
    this.collectionIndex = collectionIndex;
  }

  @Override
  public List<BoundSubject> get(DataFetchingEnvironment environment) {
    try (Stream<String> subjects = collectionIndex.getSubjects(collectionName)) {
      return subjects
        .map(BoundSubject::new)
        .limit(20)
        .collect(Collectors.toList());
    }
  }
}
