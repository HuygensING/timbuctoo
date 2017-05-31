package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorSubject;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbCollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.PaginationHelper.getPaginatedList;

public class CollectionDataFetcher implements CollectionFetcher {
  private final String collectionName;
  private final BdbCollectionIndex collectionIndex;

  public CollectionDataFetcher(String collectionName, BdbCollectionIndex collectionIndex) {
    this.collectionName = collectionName;
    this.collectionIndex = collectionIndex;
  }

  @Override
  public PaginatedList getList(PaginationArguments arguments) {
    try (Stream<CursorSubject> subjectStream = collectionIndex.getSubjects(collectionName, arguments.getCursor())) {
      return getPaginatedList(subjectStream, CursorSubject::getSubject, arguments.getCount());
    }
  }

  @Override
  public TypedValue getItem(String uri) {
    return null;
  }
}
