package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;

import java.util.ArrayList;
import java.util.List;

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
    List<BoundSubject> result = new ArrayList<>(20);
    try (AutoCloseableIterator<String> subjects = collectionIndex.getSubjects(collectionName)) {
      int counter = 0;
      while (counter++ < MAX_ITEMS && subjects.hasNext()) {
        result.add(new BoundSubject(subjects.next()));
      }
    }
    return result;
  }
}
