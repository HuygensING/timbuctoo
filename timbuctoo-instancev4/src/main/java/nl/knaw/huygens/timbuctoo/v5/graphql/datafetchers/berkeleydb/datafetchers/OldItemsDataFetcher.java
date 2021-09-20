package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.updatedperpatchstore.SubjectCursor;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.CollectionMetadata;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.StringList;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.OUT;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class OldItemsDataFetcher implements DataFetcher<StringList> {
  private final PaginationArgumentsHelper argumentsHelper;

  public OldItemsDataFetcher(PaginationArgumentsHelper argumentsHelper) {
    this.argumentsHelper = argumentsHelper;
  }

  @Override
  public StringList get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof CollectionMetadata) {
      CollectionMetadata source = environment.getSource();
      PaginationArguments arguments = argumentsHelper.getPaginationArguments(environment);
      DataSet dataSet = source.getDataSet();
      Optional<ZonedDateTime> deletedSince = arguments.getTimeSince();

      List<LogEntry> entries = dataSet.getImportManager().getLogList().getEntries();
      Optional<LogEntry> startEntry = entries.stream().filter(logEntry ->
          ZonedDateTime.parse(logEntry.getImportStatus().getDate()).isAfter(deletedSince.get())).findFirst();
      int version = startEntry.map(entries::indexOf).orElse(-1);

      List<Integer> versions = version > -1 ?
          dataSet.getUpdatedPerPatchStore().getVersions().filter(v -> v > version)
                 .sorted((v1, v2) -> -v1.compareTo(v2)).collect(Collectors.toList()) :
          Collections.emptyList();

      try (Stream<SubjectCursor> subjectStream =
               dataSet.getUpdatedPerPatchStore().fromVersion(version, arguments.getCursor())) {
        final PaginatedList<String> paginatedList = PaginationHelper.getUpdatedPaginatedList(
            subjectStream.filter(subjectCursor ->
                OldItemsDataFetcher.wasDeletedInCollection(source, versions, subjectCursor.getSubject())),
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

  private static boolean wasDeletedInCollection(CollectionMetadata source, List<Integer> versions, String subject) {
    BdbTruePatchStore store = source.getDataSet().getTruePatchStore();
    for (int version : versions) {
      if (store.getChanges(subject, RDF_TYPE, OUT, version, true).findAny().isPresent()) {
        return false;
      }
      if (store.getChanges(subject, RDF_TYPE, OUT, version, false).findAny().isPresent()) {
        return true;
      }
    }
    return false;
  }
}
