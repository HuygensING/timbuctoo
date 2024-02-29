package nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.CursorUri;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.datastores.updatedperpatchstore.SubjectCursor;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.Streams.distinctByKey;
import static nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.datafetchers.PaginationHelper.getPaginatedList;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.RDFS_RESOURCE;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.RDF_TYPE;

public class CollectionDataFetcher implements CollectionFetcher {
  private final String collectionUri;

  public CollectionDataFetcher(String collectionUri) {
    this.collectionUri = collectionUri;
  }

  @Override
  public PaginatedList<SubjectReference> getList(PaginationArguments arguments, DataSet dataSet) {
    if (arguments.getTimeSince().isPresent()) {
      return getUpdatedPaginatedList(arguments.getTimeSince().get(), arguments, dataSet);
    }

    Optional<Long> total = Optional.empty();
    if (arguments.getGraph().isEmpty() &&
        dataSet.getSchemaStore().getStableTypes() != null &&
        dataSet.getSchemaStore().getStableTypes().get(collectionUri) != null) {
      total = Optional.of(dataSet.getSchemaStore().getStableTypes().get(collectionUri).getSubjectsWithThisType());
    }

    if (collectionUri.equals(RDFS_RESOURCE)) {
      return getRdfResourcePaginatedList(total, arguments, dataSet);
    }

    return getDefaultPaginatedList(total, arguments, dataSet);
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
          subjectCursor -> new LazyTypeSubjectReference(subjectCursor.getSubject(), arguments.getGraph(), dataSet),
          arguments);
    }
  }


  private PaginatedList<SubjectReference> getRdfResourcePaginatedList(Optional<Long> total,
                                                                      PaginationArguments arguments, DataSet dataSet) {
    try (Stream<CursorUri> uriStream = dataSet.getDefaultResourcesStore().getDefaultResources(arguments.getCursor())) {
      return getPaginatedList(
          uriStream,
          cursorUri -> new LazyTypeSubjectReference(cursorUri.getUri(), arguments.getGraph(), dataSet),
          arguments,
          total
      );
    }
  }

  private PaginatedList<SubjectReference> getDefaultPaginatedList(Optional<Long> total,
                                                                  PaginationArguments arguments, DataSet dataSet) {
    try (Stream<CursorQuad> quadStream = dataSet
        .getQuadStore()
        .getQuadsInGraph(collectionUri, RDF_TYPE, Direction.IN, arguments.getCursor(), arguments.getGraph(), true)
        .filter(distinctByKey(CursorQuad::getObject))) {
      return PaginationHelper.getPaginatedList(
          quadStream,
          cursorSubject -> new LazyTypeSubjectReference(cursorSubject.getObject(), arguments.getGraph(), dataSet),
          arguments,
          total
      );
    }
  }

  private boolean isSubjectInCollection(DataSet dataSet, String subject) {
    try (Stream<CursorQuad> stream = dataSet.getQuadStore().getQuads(subject, RDF_TYPE, Direction.OUT, "")) {
      return stream.anyMatch(quad -> quad.getObject().equals(collectionUri));
    }
  }
}
