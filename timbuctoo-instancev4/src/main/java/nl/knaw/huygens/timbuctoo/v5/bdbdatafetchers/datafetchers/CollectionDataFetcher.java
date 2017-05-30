package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbCollectionIndex;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionDataFetcher implements DataFetcher {
  private final String collectionName;
  private final BdbCollectionIndex collectionIndex;

  public CollectionDataFetcher(String collectionName, BdbCollectionIndex collectionIndex) {
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
