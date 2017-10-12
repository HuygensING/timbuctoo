package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CursorSubject;
import nl.knaw.huygens.timbuctoo.v5.graphql.collectionfilter.FilterResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.PaginationHelper
  .getPaginatedList;

public class CollectionDataFetcher implements CollectionFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(CollectionDataFetcher.class);

  private final String collectionUri;

  public CollectionDataFetcher(String collectionUri) {
    this.collectionUri = collectionUri;
  }

  @Override
  public PaginatedList<SubjectReference> getList(PaginationArguments arguments, DataSet dataSet) {
    String cursor = arguments.getCursor();
    if (arguments.getFilter().isPresent()) {
      try {
        final FilterResult result = arguments.getFilter().get().query();
        return PaginatedList.create(
          null,
          result.getNextToken(),
          result.getUriList().stream().map(x -> new LazyTypeSubjectReference(x, dataSet)).collect(Collectors.toList()),
          Optional.of(result.getTotal()),
          result.getFacets()
        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      try (Stream<CursorSubject> subjectStream = dataSet.getCollectionIndex().getSubjects(collectionUri, cursor)) {
        return getPaginatedList(
          subjectStream,
          cursorSubject -> new LazyTypeSubjectReference(cursorSubject.getSubjectUri(), dataSet),
          arguments
        );
      }
    }
  }

}
