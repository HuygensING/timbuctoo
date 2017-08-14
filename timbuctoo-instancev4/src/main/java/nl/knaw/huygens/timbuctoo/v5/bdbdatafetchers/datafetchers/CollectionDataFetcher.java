package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorSubject;
import nl.knaw.huygens.timbuctoo.v5.dataset.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.PaginationHelper.getPaginatedList;

public class CollectionDataFetcher implements CollectionFetcher {
  private final String collectionName;
  private final CollectionIndex collectionIndex;

  public CollectionDataFetcher(String collectionName, CollectionIndex collectionIndex) {
    this.collectionName = collectionName;
    this.collectionIndex = collectionIndex;
  }

  @Override
  public PaginatedList<SubjectReference> getList(PaginationArguments arguments) {
    String cursor = arguments.getCursor();
    try (Stream<CursorSubject> subjectStream = collectionIndex.getSubjects(collectionName, cursor)) {
      return getPaginatedList(subjectStream, CursorSubject::getSubject, arguments.getCount(), !cursor.isEmpty());
    }
  }

}
