package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Stopwatch;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.ChangeFetcher;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.OptimizedPatchListener;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.ImportStatusLabel;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.rssource.ChangeListBuilder;
import nl.knaw.huygens.timbuctoo.v5.filestorage.ChangeLogStorage;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.OUT;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.LANGSTRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

public class StoreUpdater implements RdfProcessor {
  private static final Logger LOG = getLogger(StoreUpdater.class);

  private final BdbQuadStore quadStore;
  private final GraphStore graphStore;
  private final BdbTypeNameStore typeNameStore;
  private final BdbPatchVersionStore patchVersionStore;
  private final UpdatedPerPatchStore updatedPerPatchStore;
  private final OldSubjectTypesStore oldSubjectTypesStore;

  private final List<OptimizedPatchListener> listeners;
  private final ImportStatus importStatus;
  private final ChangeLogStorage changeLogStorage;

  private int version = -1;
  private Stopwatch stopwatch;
  private long count;
  private long prevCount;
  private long prevTime;
  private String logString;
  private boolean hasOldSubjectTypes;

  public StoreUpdater(BdbQuadStore quadStore, GraphStore graphStore,
                      BdbTypeNameStore typeNameStore, BdbPatchVersionStore patchVersionStore,
                      UpdatedPerPatchStore updatedPerPatchStore, OldSubjectTypesStore oldSubjectTypesStore,
                      List<OptimizedPatchListener> listeners, ImportStatus importStatus,
                      ChangeLogStorage changeLogStorage) {
    this.quadStore = quadStore;
    this.graphStore = graphStore;
    this.typeNameStore = typeNameStore;
    this.patchVersionStore = patchVersionStore;
    this.updatedPerPatchStore = updatedPerPatchStore;
    this.oldSubjectTypesStore = oldSubjectTypesStore;
    this.listeners = listeners;
    this.importStatus = importStatus;
    this.changeLogStorage = changeLogStorage;
  }

  private void updateListeners() throws RdfProcessingFailedException {
    // start updating the derivative stores
    logString = "Processed {} subjects ({} subjects/s)";
    for (OptimizedPatchListener listener : listeners) {
      listener.start();
      importStatus.startProgressItem(listener.getClass().getSimpleName());
    }

    count = 0;
    prevCount = 0;
    prevTime = stopwatch.elapsed(TimeUnit.SECONDS);
    try (Stream<String> subjects = updatedPerPatchStore.ofVersion(version)) {
      final ChangeFetcher getQuads = new ChangeFetcherImpl(patchVersionStore, quadStore);
      final Iterator<String> iterator = subjects.iterator();
      while (iterator.hasNext()) {
        final boolean needUpdate = notifyUpdate();

        final String subject = iterator.next();
        for (OptimizedPatchListener listener : listeners) {
          if (needUpdate) {
            listener.notifyUpdate();
            importStatus.updateProgressItem(listener.getClass().getSimpleName(), count);
          }
          listener.onChangedSubject(subject, getQuads);
        }
      }
    }
    for (OptimizedPatchListener listener : listeners) {
      listener.finish();
      importStatus.finishProgressItem(listener.getClass().getSimpleName());
    }
  }

  private void buildChangeLog() throws RdfProcessingFailedException {
    try {
      try (OutputStream out = changeLogStorage.getChangeLogOutputStream(version)) {
        ChangeListBuilder changeListBuilder = new ChangeListBuilder();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        try (Stream<CursorQuad> quads = patchVersionStore.retrieveChanges();
             Stream<String> data = changeListBuilder.retrieveChanges(quads)) {
          for (Iterator<String> dataIt = data.iterator(); dataIt.hasNext(); ) {
            writer.write(dataIt.next());
          }
        }
        writer.flush();
      }

      patchVersionStore.empty();
      patchVersionStore.commit();
    } catch (IOException ioe) {
      throw new RdfProcessingFailedException(ioe);
    }
  }

  @Override
  public void setPrefix(String prefix, String iri) throws RdfProcessingFailedException {
    notifyUpdate();
    typeNameStore.addPrefix(prefix, iri);
  }

  private void putQuad(String subject, String predicate, Direction direction, String object, String valueType,
                       String language, String graph) throws RdfProcessingFailedException {
    try {
      final boolean wasChanged = quadStore.putQuad(subject, predicate, direction, object, valueType, language, graph);
      if (wasChanged) {
        patchVersionStore.put(subject, predicate, direction, true, object, valueType, language, graph);
        updatedPerPatchStore.put(version, subject);
        graphStore.put(graph, subject);

        if (hasOldSubjectTypes && predicate.equals(RDF_TYPE)) {
          for (int v = 0; v < version; v++) {
            oldSubjectTypesStore.delete(subject, object, v);
          }
        }
      }
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  private void deleteQuad(String subject, String predicate, Direction direction, String object, String valueType,
                          String language, String graph) throws RdfProcessingFailedException {
    try {
      final boolean wasChanged =
          quadStore.deleteQuad(subject, predicate, direction, object, valueType, language, graph);
      if (wasChanged) {
        patchVersionStore.put(subject, predicate, direction, false, object, valueType, language, graph);
        updatedPerPatchStore.put(version, subject);

        if (predicate.equals(RDF_TYPE)) {
          oldSubjectTypesStore.put(subject, object, version);
        }

        try (Stream<CursorQuad> quadStream = quadStore.getQuads(subject)) {
          if (quadStream.noneMatch(quad -> quad.getGraph().isPresent() && quad.getGraph().get().equals(graph))) {
            graphStore.delete(graph, subject);
          }
        }
      }
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void addRelation(String subject, String predicate, String object, String graph)
    throws RdfProcessingFailedException {
    notifyUpdate();
    putQuad(subject, predicate, OUT, object, null, null, graph);
    putQuad(object, predicate, Direction.IN, subject, null, null, graph);
  }

  @Override
  public void addValue(String subject, String predicate, String value, String dataType, String graph)
    throws RdfProcessingFailedException {
    notifyUpdate();
    putQuad(subject, predicate, OUT, value, dataType, null, graph);
  }

  @Override
  public void addLanguageTaggedString(String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {
    notifyUpdate();
    putQuad(subject, predicate, OUT, value, LANGSTRING, language, graph);
  }

  @Override
  public void delRelation(String subject, String predicate, String object, String graph)
    throws RdfProcessingFailedException {
    notifyUpdate();
    deleteQuad(subject, predicate, OUT, object, null, null, graph);
    deleteQuad(object, predicate, Direction.IN, subject, null, null, graph);
  }

  @Override
  public void delValue(String subject, String predicate, String value, String dataType, String graph)
    throws RdfProcessingFailedException {
    notifyUpdate();
    deleteQuad(subject, predicate, OUT, value, dataType, null, graph);
  }

  @Override
  public void delLanguageTaggedString(String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {
    notifyUpdate();
    deleteQuad(subject, predicate, OUT, value, LANGSTRING, language, graph);
  }


  @Override
  public void start(int index) throws RdfProcessingFailedException {
    stopwatch = Stopwatch.createStarted();
    version = index;
    startTransactions();
    logString = "Processed {} triples ({} triples/s)";
    count = 0; // reset the count to make sure the right amount of imported triples are logged.
    hasOldSubjectTypes = oldSubjectTypesStore.size() > 0;
  }

  private void startTransactions() {
    quadStore.start();
    importStatus.addProgressItem(BdbQuadStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    graphStore.start();
    importStatus.addProgressItem(GraphStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    patchVersionStore.start();
    importStatus.addProgressItem(BdbPatchVersionStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    typeNameStore.start();
    importStatus.addProgressItem(BdbTypeNameStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    updatedPerPatchStore.start();
    importStatus.addProgressItem(UpdatedPerPatchStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    oldSubjectTypesStore.start();
    importStatus.addProgressItem(OldSubjectTypesStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);

    listeners.forEach(listener -> importStatus.addProgressItem(
      listener.getClass().getSimpleName(), ImportStatusLabel.PENDING)
    );
  }

  private boolean notifyUpdate() {
    count++;
    final long curTime = stopwatch.elapsed(TimeUnit.SECONDS);
    if (curTime - prevTime > 5) {
      final long itemsPerSecond = (count - prevCount) / (curTime - prevTime);
      LOG.info(logString, count, itemsPerSecond);
      importStatus.setStatus(String.format(logString.replaceAll("\\{\\}", "%d"),
        count, itemsPerSecond));
      prevCount = count;
      prevTime = curTime;
      updateStoreProgress();
      return true;
    }
    return false;
  }

  private void updateStoreProgress() {
    importStatus.updateProgressItem(BdbQuadStore.class.getSimpleName(), count);
    importStatus.updateProgressItem(GraphStore.class.getSimpleName(), count);
    importStatus.updateProgressItem(BdbPatchVersionStore.class.getSimpleName(), count);
    importStatus.updateProgressItem(BdbTypeNameStore.class.getSimpleName(), count);
    importStatus.updateProgressItem(UpdatedPerPatchStore.class.getSimpleName(), count);
    importStatus.updateProgressItem(OldSubjectTypesStore.class.getSimpleName(), count);
  }

  @Override
  public int getCurrentVersion() {
    return version;
  }

  @Override
  public void commit() throws RdfProcessingFailedException {
    try {
      String msg = "processing " + count + " triples took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds";
      LOG.info(msg);
      importStatus.setStatus(msg);
      importStatus.setStatus(msg);
      stopwatch.reset();
      stopwatch.start();
      commitChanges();
      msg = "committing took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds";
      LOG.info(msg);
      stopwatch.reset();
      stopwatch.start();
      updateListeners();
      msg = "post-processing took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds";
      LOG.info(msg);
      stopwatch.reset();
      stopwatch.start();
      buildChangeLog();
      msg = "writing changelog took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds";
      LOG.info(msg);
      importStatus.setStatus(msg);
    } catch (DatabaseWriteException | JsonProcessingException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  private void commitChanges() throws JsonProcessingException, DatabaseWriteException {
    typeNameStore.commit();
    importStatus.finishProgressItem(BdbTypeNameStore.class.getSimpleName());
    quadStore.commit();
    importStatus.finishProgressItem(BdbQuadStore.class.getSimpleName());
    graphStore.commit();
    importStatus.finishProgressItem(GraphStore.class.getSimpleName());
    patchVersionStore.commit();
    importStatus.finishProgressItem(BdbPatchVersionStore.class.getSimpleName());
    updatedPerPatchStore.commit();
    importStatus.finishProgressItem(UpdatedPerPatchStore.class.getSimpleName());
    oldSubjectTypesStore.commit();
    importStatus.finishProgressItem(OldSubjectTypesStore.class.getSimpleName());
  }
}
