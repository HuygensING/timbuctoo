package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorSubject;
import nl.knaw.huygens.timbuctoo.v5.dataset.SubjectStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.PaginationHelper.getPaginatedList;

public class CollectionDataFetcher implements CollectionFetcher {
  private final String collectionName;
  private final SubjectStore collectionIndex;

  public CollectionDataFetcher(String collectionName, SubjectStore collectionIndex) {
    this.collectionName = collectionName;
    this.collectionIndex = collectionIndex;
  }

  @Override
  public PaginatedList getList(PaginationArguments arguments) {
    try (Stream<CursorSubject> subjectStream = collectionIndex.getSubjects(collectionName, arguments.getCursor())) {
      return getPaginatedList(subjectStream, CursorSubject::getSubject, arguments.getCount());
    }
  }

}
