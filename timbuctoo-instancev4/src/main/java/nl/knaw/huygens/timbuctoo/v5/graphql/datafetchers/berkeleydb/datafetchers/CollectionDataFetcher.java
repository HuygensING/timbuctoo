package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import graphql.GraphQLError;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CursorSubject;
import nl.knaw.huygens.timbuctoo.v5.elasticsearch.ElasticSearch;
import nl.knaw.huygens.timbuctoo.v5.elasticsearch.PageableResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.PaginationHelper
  .getPaginatedList;

public class CollectionDataFetcher implements CollectionFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(CollectionDataFetcher.class);

  private final String collectionName;
  private final ElasticSearch elasticSearch;

  public CollectionDataFetcher(String collectionName, ElasticSearch elasticSearch) {
    this.collectionName = collectionName;
    this.elasticSearch = elasticSearch;
  }

  @Override
  public PaginatedList<SubjectReference> getList(PaginationArguments arguments, DataSet dataSet) {
    String cursor = arguments.getCursor();
    if (arguments.getSearchQuery().isPresent()) {
      try {
        final PageableResult result = elasticSearch.query(
          collectionName,
          arguments.getSearchQuery().get(),
          cursor,
          arguments.getCount()
        );
        return PaginatedList.create(
          null,
          result.getToken(),
          result.getIdList().stream().map(x -> new LazyTypeSubjectReference(x, dataSet)).collect(Collectors.toList())
        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
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

}
