package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.google.common.base.Stopwatch;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.ChangeFetcher;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.OptimizedPatchListener;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.OUT;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.LANGSTRING;
import static org.slf4j.LoggerFactory.getLogger;

public class StoreUpdater implements RdfProcessor {

  private static final Logger LOG = getLogger(StoreUpdater.class);
  private final BdbEnvironmentCreator dbFactory;
  private final BdbTripleStore tripleStore;
  private final BdbTypeNameStore typeNameStore;
  private final BdbTruePatchStore truePatchStore;
  private final UpdatedPerPatchStore updatedPerPatchStore;
  private final VersionStore versionStore;
  private int currentversion = -1;
  private Stopwatch stopwatch;
  private long count;
  private long prevCount;
  private List<OptimizedPatchListener> listeners;
  private long prevTime;
  private String logString;
  private ImportStatus importStatus;

  public StoreUpdater(BdbEnvironmentCreator dbFactory, BdbTripleStore tripleStore, BdbTypeNameStore typeNameStore,
                      BdbTruePatchStore truePatchStore, UpdatedPerPatchStore updatedPerPatchStore,
                      List<OptimizedPatchListener> listeners,
                      VersionStore versionStore, ImportStatus importStatus) {
    this.dbFactory = dbFactory;
    this.tripleStore = tripleStore;
    this.typeNameStore = typeNameStore;
    this.truePatchStore = truePatchStore;
    this.updatedPerPatchStore = updatedPerPatchStore;
    this.versionStore = versionStore;
    currentversion = versionStore.getVersion();
    this.listeners = listeners;
    this.importStatus = importStatus;
  }

  private void updateListeners() throws RdfProcessingFailedException {
    logString = "Processed {} subjects ({} subjects/s)";
    for (OptimizedPatchListener listener : listeners) {
      listener.start();
    }

    count = 0;
    prevCount = 0;
    prevTime = stopwatch.elapsed(TimeUnit.SECONDS);
    try (Stream<String> subjects = updatedPerPatchStore.ofVersion(currentversion)) {
      final ChangeFetcher getQuads = new ChangeFetcherImpl(truePatchStore, tripleStore, currentversion);
      final Iterator<String> iterator = subjects.iterator();
      while (iterator.hasNext()) {
        final boolean needUpdate = notifyUpdate();

        final String subject = iterator.next();
        for (OptimizedPatchListener listener : listeners) {
          if (needUpdate) {
            listener.notifyUpdate();
          }
          listener.onChangedSubject(subject, getQuads);
        }
      }
    }
    for (OptimizedPatchListener listener : listeners) {
      listener.finish();
    }
  }

  @Override
  public void setPrefix(String prefix, String iri) throws RdfProcessingFailedException {
    notifyUpdate();
    typeNameStore.addPrefix(prefix, iri);
  }

  private void putQuad(String subject, String predicate, Direction direction, String object, String valueType,
                       String language) throws RdfProcessingFailedException {
    try {
      final boolean wasChanged = tripleStore.putQuad(subject, predicate, direction, object, valueType, language);
      if (wasChanged && currentversion >= 0) {
        truePatchStore.put(subject, currentversion, predicate, direction, true, object, valueType, language);
        updatedPerPatchStore.put(currentversion, subject);
      }
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  private void deleteQuad(String subject, String predicate, Direction direction, String object, String valueType,
                          String language) throws RdfProcessingFailedException {
    try {
      final boolean wasChanged = tripleStore.deleteQuad(subject, predicate, direction, object, valueType, language);
      if (wasChanged && currentversion >= 0) {
        truePatchStore.put(subject, currentversion, predicate, direction, false, object, valueType, language);
        updatedPerPatchStore.put(currentversion, subject);
      }
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void addRelation(String subject, String predicate, String object, String graph)
    throws RdfProcessingFailedException {
    notifyUpdate();
    putQuad(subject, predicate, OUT, object, null, null);
    putQuad(object, predicate, Direction.IN, subject, null, null);
  }

  @Override
  public void addValue(String subject, String predicate, String value, String dataType, String graph)
    throws RdfProcessingFailedException {
    notifyUpdate();
    putQuad(subject, predicate, OUT, value, dataType, null);
  }

  @Override
  public void addLanguageTaggedString(String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {
    notifyUpdate();
    putQuad(subject, predicate, OUT, value, LANGSTRING, language);
  }

  @Override
  public void delRelation(String subject, String predicate, String object, String graph)
    throws RdfProcessingFailedException {
    notifyUpdate();
    deleteQuad(subject, predicate, OUT, object, null, null);
    deleteQuad(object, predicate, Direction.IN, subject, null, null);
  }

  @Override
  public void delValue(String subject, String predicate, String value, String dataType, String graph)
    throws RdfProcessingFailedException {
    notifyUpdate();
    deleteQuad(subject, predicate, OUT, value, dataType, null);
  }

  @Override
  public void delLanguageTaggedString(String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {
    notifyUpdate();
    deleteQuad(subject, predicate, OUT, value, LANGSTRING, language);
  }


  @Override
  public void start(int index) throws RdfProcessingFailedException {
    stopwatch = Stopwatch.createStarted();
    currentversion = index;
    dbFactory.startTransaction();
    logString = "Processed {} triples ({} triples/s)";
  }

  private boolean notifyUpdate() {
    count++;
    final long curTime = stopwatch.elapsed(TimeUnit.SECONDS);
    if (curTime - prevTime > 5) {
      final long itemsPerSecond = (count - prevCount) / (curTime - prevTime);
      LOG.info(logString, count, itemsPerSecond);
      importStatus.addMessage(String.format(logString.replaceAll("\\{\\}", "%d"),
        count, itemsPerSecond));
      prevCount = count;
      prevTime = curTime;
      return true;
    }
    return false;
  }

  @Override
  public int getCurrentVersion() {
    return currentversion;
  }

  @Override
  public void commit() throws RdfProcessingFailedException {
    try {
      String msg = "processing " + count + " triples took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds";
      LOG.info(msg);
      importStatus.addMessage(msg);
      stopwatch.reset();
      stopwatch.start();
      updateListeners();
      msg = "post-processing took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds";
      LOG.info(msg);
      importStatus.addMessage(msg);
      stopwatch.reset();
      stopwatch.start();
      versionStore.setVersion(currentversion);
      dbFactory.commitTransaction();
      msg = "committing took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds";
      LOG.info(msg);
      importStatus.addMessage(msg);
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

}
