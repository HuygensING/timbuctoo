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
import org.slf4j.Logger;

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
  private final BdbTruePatchStore truePatchStore;
  private final UpdatedPerPatchStore updatedPerPatchStore;
  private final OldSubjectTypesStore oldSubjectTypesStore;

  private final List<OptimizedPatchListener> listeners;
  private final ImportStatus importStatus;

  private int version = -1;
  private Stopwatch stopwatch;
  private long count;
  private long prevCount;
  private long prevTime;
  private String logString;

  public StoreUpdater(BdbQuadStore quadStore, GraphStore graphStore,
                      BdbTypeNameStore typeNameStore, BdbTruePatchStore truePatchStore,
                      UpdatedPerPatchStore updatedPerPatchStore, List<OptimizedPatchListener> listeners,
                      VersionStore versionStore, ImportStatus importStatus) {
    this.quadStore = quadStore;
    this.graphStore = graphStore;
    this.typeNameStore = typeNameStore;
    this.truePatchStore = truePatchStore;
    this.updatedPerPatchStore = updatedPerPatchStore;
    this.oldSubjectTypesStore = oldSubjectTypesStore;
    this.listeners = listeners;
    this.importStatus = importStatus;
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
      final ChangeFetcher getQuads = new ChangeFetcherImpl(truePatchStore, quadStore, version);
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
        truePatchStore.put(subject, version, predicate, direction, true, object, valueType, language);
        updatedPerPatchStore.put(version, subject);
        if (predicate.equals(RDF_TYPE)) {
          for (int v = 0; v < version; v++) {
            oldSubjectTypesStore.delete(subject, object, v);
          }
        }

        truePatchStore.put(subject, currentversion, predicate, direction,
            true, object, valueType, language, graph);
        updatedPerPatchStore.put(currentversion, subject);
        graphStore.put(graph, subject);
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
        truePatchStore.put(subject, version, predicate, direction, false, object, valueType, language);
        updatedPerPatchStore.put(version, subject);
        if (predicate.equals(RDF_TYPE)) {
          oldSubjectTypesStore.put(subject, object, version);
        }
        truePatchStore.put(subject, currentversion, predicate, direction,
            false, object, valueType, language, graph);
        updatedPerPatchStore.put(currentversion, subject);

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
  }

  private void startTransactions() {
    versionStore.start();
    importStatus.addProgressItem(VersionStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    quadStore.start();
    importStatus.addProgressItem(BdbQuadStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    graphStore.start();
    importStatus.addProgressItem(GraphStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    truePatchStore.start();
    importStatus.addProgressItem(BdbTruePatchStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
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
    importStatus.updateProgressItem(BdbTruePatchStore.class.getSimpleName(), count);
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
    truePatchStore.commit();
    importStatus.finishProgressItem(BdbTruePatchStore.class.getSimpleName());
    updatedPerPatchStore.commit();
    importStatus.finishProgressItem(UpdatedPerPatchStore.class.getSimpleName());
    oldSubjectTypesStore.commit();
    importStatus.finishProgressItem(OldSubjectTypesStore.class.getSimpleName());
  }
}
