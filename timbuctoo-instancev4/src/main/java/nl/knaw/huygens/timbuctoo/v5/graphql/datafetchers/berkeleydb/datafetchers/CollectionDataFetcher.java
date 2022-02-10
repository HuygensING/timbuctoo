package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.updatedperpatchstore.SubjectCursor;
import nl.knaw.huygens.timbuctoo.v5.graphql.collectionfilter.FilterResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.IN;
import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.OUT;
import static nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.PaginationHelper.getPaginatedList;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class CollectionDataFetcher implements CollectionFetcher {
  private final String collectionUri;

  public CollectionDataFetcher(String collectionUri) {
    this.collectionUri = collectionUri;
  }

  @Override
  public PaginatedList<SubjectReference> getList(PaginationArguments arguments, DataSet dataSet) {
    try {
      if (arguments.getFilter().isPresent()) {
        return getFilteredPaginatedList(arguments.getFilter().get().query(), dataSet);
      }

      if (arguments.getTimeSince().isPresent()) {
        return getUpdatedPaginatedList(arguments.getTimeSince().get(), arguments, dataSet);
      }

      return getDefaultPaginatedList(arguments, dataSet);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PaginatedList<SubjectReference> getList(PaginationArguments arguments, DataSet dataSet) {
    String cursor = arguments.getCursor();
    if (arguments.getFilter().isPresent()) {
      try {
        final FilterResult result = arguments.getFilter().get().query();
        return PaginatedList.create(
            result.getPrevToken(),
            result.getNextToken(),
            result.getUriList().stream()
                  .map(x -> new LazyTypeSubjectReference(x, arguments.getGraph(), dataSet)).collect(Collectors.toList()),
            Optional.of((long) result.getTotal()),
            result.getFacets()
        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      try (Stream<CursorQuad> subjectStream =
               dataSet.getQuadStore().getQuadsInGraph(collectionUri, RDF_TYPE, IN, cursor, arguments.getGraph())) {
        Optional<Long> total = Optional.empty();
        if (dataSet.getSchemaStore().getStableTypes() != null &&
            dataSet.getSchemaStore().getStableTypes().get(collectionUri) != null) {

          total = Optional.of(
              dataSet.getSchemaStore().getStableTypes().get(collectionUri).getSubjectsWithThisType()
          );
        }
        return getPaginatedList(
            subjectStream,
            cursorSubject -> new LazyTypeSubjectReference(cursorSubject.getObject(), arguments.getGraph(), dataSet),
            arguments,
            total
        );
      }
    }
  }

  private PaginatedList<SubjectReference> getFilteredPaginatedList(FilterResult result, DataSet dataSet) {
    return PaginatedList.create(
        result.getPrevToken(),
        result.getNextToken(),
        result.getUriList().stream()
              .map(x -> new LazyTypeSubjectReference(x, dataSet))
              .collect(Collectors.toList()),
        Optional.of((long) result.getTotal()),
        result.getFacets()
    );
  }

  private PaginatedList<SubjectReference> getUpdatedPaginatedList(ZonedDateTime updatedSince,
                                                                  PaginationArguments arguments, DataSet dataSet) {
    List<LogEntry> entries = dataSet.getImportManager().getLogList().getEntries();
    Optional<LogEntry> startEntry = entries.stream().filter(logEntry ->
        ZonedDateTime.parse(logEntry.getImportStatus().getDate()).isAfter(updatedSince)).findFirst();
    int version = startEntry.map(entries::indexOf).orElse(-1);

    try (Stream<SubjectCursor> subjectStream =
             dataSet.getUpdatedPerPatchStore().fromVersion(version, arguments.getCursor())) {
      return PaginationHelper.getUpdatedPaginatedList(
        subjectStream.filter(subjectCursor -> this.isSubjectInCollection(dataSet, subjectCursor.getSubject())),
        subjectCursor -> new LazyTypeSubjectReference(subjectCursor.getSubject(), dataSet),
        arguments);
    }
  }

  private PaginatedList<SubjectReference> getDefaultPaginatedList(PaginationArguments arguments, DataSet dataSet) {
    try (Stream<CursorQuad> subjectStream =
             dataSet.getQuadStore().getQuads(collectionUri, RDF_TYPE, IN, arguments.getCursor())) {
      Optional<Long> total = Optional.empty();
      if (dataSet.getSchemaStore().getStableTypes() != null &&
          dataSet.getSchemaStore().getStableTypes().get(collectionUri) != null) {
        total = Optional.of(dataSet.getSchemaStore().getStableTypes().get(collectionUri).getSubjectsWithThisType());
      }

      return PaginationHelper.getPaginatedList(
        subjectStream,
        cursorSubject -> new LazyTypeSubjectReference(cursorSubject.getObject(), arguments.getGraph(), dataSet),
        arguments,
        total);
    }
  }

  private boolean isSubjectInCollection(DataSet dataSet, String subject) {
    try (Stream<CursorQuad> stream = dataSet.getQuadStore().getQuads(subject, RDF_TYPE, OUT, "")) {
      return stream.anyMatch(quad -> quad.getObject().equals(collectionUri));
    }
  }
}
