package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CursorSubject;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.PaginationHelper.getPaginatedList;

public class CollectionDataFetcher implements CollectionFetcher {
  private final String collectionName;

  public CollectionDataFetcher(String collectionName) {
    this.collectionName = collectionName;
  }

  @Override
  public PaginatedList<SubjectReference> getList(PaginationArguments arguments, DataSet dataSet) {
    String cursor = arguments.getCursor();
    try (Stream<CursorSubject> subjectStream = dataSet.getCollectionIndex().getSubjects(collectionName, cursor)) {
      return getPaginatedList(
        subjectStream,
        cursorSubject -> new LazyTypeSubjectReference(cursorSubject.getSubjectUri(), dataSet),
        arguments.getCount(),
        !cursor.isEmpty()
      );
    }
  }

}
