package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.v5.datastores.updatedperpatchstore.SubjectCursor;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.CollectionMetadata;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.StringList;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class OldItemsDataFetcher implements DataFetcher<StringList> {
  private final PaginationArgumentsHelper argumentsHelper;

  public OldItemsDataFetcher(PaginationArgumentsHelper argumentsHelper) {
    this.argumentsHelper = argumentsHelper;
  }

  @Override
  public StringList get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof CollectionMetadata) {
      CollectionMetadata source = environment.getSource();
      DataSet dataSet = source.getDataSet();
      PaginationArguments arguments = argumentsHelper.getPaginationArguments(environment);
      Optional<ZonedDateTime> deletedSince = arguments.getTimeSince();

      List<LogEntry> entries = dataSet.getImportManager().getLogList().getEntries();
      Optional<LogEntry> startEntry = entries.stream().filter(logEntry ->
          ZonedDateTime.parse(logEntry.getImportStatus().getDate()).isAfter(deletedSince.get())).findFirst();
      int version = startEntry.map(entries::indexOf).orElse(-1);

      String cursor = arguments.getCursor();
      try (Stream<SubjectCursor> subjectStream =
               dataSet.getOldSubjectTypesStore().fromTypeAndVersion(source.getSubjectUri(), version, cursor)) {
        final PaginatedList<String> paginatedList = PaginationHelper.getUpdatedPaginatedList(
            subjectStream,
            SubjectCursor::getSubject,
            arguments);

        return StringList.create(
            paginatedList.getPrevCursor(),
            paginatedList.getNextCursor(),
            paginatedList.getItems());
      }
    }

    return StringList.create(
        Optional.empty(),
        Optional.empty(),
        Lists.newArrayList());
  }
}
