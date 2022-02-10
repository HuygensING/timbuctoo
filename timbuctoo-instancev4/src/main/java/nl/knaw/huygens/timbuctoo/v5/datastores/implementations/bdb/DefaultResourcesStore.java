package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.DatabaseGetter;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.ChangeFetcher;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.OptimizedPatchListener;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.berkeleydb.DatabaseGetter.Iterate.BACKWARDS;
import static nl.knaw.huygens.timbuctoo.v5.berkeleydb.DatabaseGetter.Iterate.FORWARDS;
import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.OUT;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class DefaultResourcesStore implements OptimizedPatchListener {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultResourcesStore.class);

  private final BdbWrapper<String, String> bdbWrapper;
  private final ImportStatus importStatus;

  public DefaultResourcesStore(BdbWrapper<String, String> bdbWrapper, ImportStatus importStatus) throws DataStoreCreationException {
    this.bdbWrapper = bdbWrapper;
    this.importStatus = importStatus;
  }

  public void putIri(String iri) throws DatabaseWriteException {
    bdbWrapper.put("iris", iri);
  }

  public void deleteIri(String iri) throws DatabaseWriteException {
    bdbWrapper.delete("iris", iri);
  }

  public Stream<CursorUri> getDefaultResources(String cursor) {
    final DatabaseGetter<String, String> getter;
    if (cursor.isEmpty()) {
      getter = bdbWrapper.databaseGetter()
                         .key("iris")
                         .dontSkip()
                         .forwards();
    } else if (cursor.equals("LAST")) {
      getter = bdbWrapper.databaseGetter()
                         .key("iris")
                         .skipToEnd()
                         .backwards();
    } else {
      getter = bdbWrapper.databaseGetter()
                         .key("iris")
                         .skipToValue(cursor.substring(2))
                         .skipOne() //we start after the cursor
                         .direction(cursor.startsWith("A\n") ? FORWARDS : BACKWARDS);
    }

    return getter.getValues(bdbWrapper.valueRetriever()).map(value -> CursorUri.create(value, value));
  }

  public void close() {
    try {
      bdbWrapper.close();
    } catch (Exception e) {
      LOG.error("Exception closing DefaultResourcesStore", e);
    }
  }

  public void commit() {
    bdbWrapper.commit();
  }

  @Override
  public void start() {
    bdbWrapper.beginTransaction();
  }

  @Override
  public void notifyUpdate() {
    importStatus.setStatus("Determining default resources (resources without a rdf type)");
  }

  @Override
  public void onChangedSubject(String subject, ChangeFetcher changeFetcher) throws RdfProcessingFailedException {
    try (Stream<CursorQuad> subjectQuads = changeFetcher.getPredicates(subject, false, true, true)) {
      if (subjectQuads.findAny().isPresent()) {
        try (Stream<CursorQuad> rdfTypeQuads =
                 changeFetcher.getPredicates(subject, RDF_TYPE, OUT, false, true, true)) {
          if (rdfTypeQuads.findFirst().isEmpty()) {
            putIri(subject);
            return;
          }
        }
      }
      deleteIri(subject);
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void finish() throws RdfProcessingFailedException {
    bdbWrapper.commit();
  }

  public boolean isClean() {
    return bdbWrapper.isClean();
  }

  public void empty() {
    bdbWrapper.empty();
  }
}
