package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.datastores.DataStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatus;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore.LogStorage;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.ProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfreader.RdfParser;
import nl.knaw.huygens.timbuctoo.v5.util.ThroughputLogger;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.slf4j.LoggerFactory.getLogger;

public class ImportManager {

  private final RdfParser rdfParser;
  private final DataStoreFactory dataStoreFactory;
  private final ExecutorService executorService;
  private static final Logger LOG = getLogger(ImportManager.class);

  public ImportManager(RdfParser rdfParser, DataStoreFactory dataStoreFactory, ExecutorService executorService) {
    this.rdfParser = rdfParser;
    this.dataStoreFactory = dataStoreFactory;
    this.executorService = executorService;
    dataStoreFactory.onDataSetsAvailable(sets -> {
      for (String set : sets) {
        scheduleLogSync(set);
      }
    });
  }

  public Future<?> addLog(String dataSet, URI identifier, Optional<String> mimeType, Optional<Charset> charset,
                          InputStream rdfInputStream)
      throws LogStorageFailedException, LogProcessingFailedException, IOException {
    DataStores dataStores = dataStoreFactory.getDataStores(dataSet);
    LogStorage logStorage = dataStores.getLogStorage();

    logStorage.saveLog(identifier, mimeType, charset, rdfInputStream);
    return scheduleLogSync(dataSet);
  }

  private void trackLogIfNeeded(DataStore<QuadLoader> store, QuadLoader updater, long version)
      throws ProcessingFailedException {
    if (store.getStatus().getCurrentVersion() < version) {
      store.process(updater, version);
    }
  }

  private Future<?> scheduleLogSync(String dataSet) {
    return executorService.submit(() -> {
      try {
        DataStores dataStores = dataStoreFactory.getDataStores(dataSet);
        for (DataSetLogEntry entry : dataStores.getLogStorage().getLogsFrom(dataStores.getCurrentVersion())) {
          MultiQuadHandler logProcessor = new MultiQuadHandler(new ThroughputLogger(10, dataSet));

          trackLogIfNeeded(dataStores.getTripleStore(), logProcessor, entry.getVersion());
          trackLogIfNeeded(dataStores.getCollectionIndex(), logProcessor, entry.getVersion());
          trackLogIfNeeded(dataStores.getTypeNameStore(), logProcessor, entry.getVersion());

          if (logProcessor.hasSubscriptions()) {
            LocalData log = entry.getData();
            rdfParser.loadFile(log.getName(), log.getMimeType(), log.getReader(), logProcessor);
          }

          if (dataStores.getSchemaStore().getStatus().getCurrentVersion() < entry.getVersion()) {
            dataStores.getSchemaStore().process(dataStores.getTripleStore(), entry.getVersion());
          }
        }
      } catch (LogProcessingFailedException | ProcessingFailedException | IOException e) {
        LOG.error("Log processing failed", e.getCause());//FIXME! add retry logic
      }
    });
  }

  public Map<String, StoreStatus> getStatus(String dataSet) throws IOException {
    return dataStoreFactory.getDataStores(dataSet).getStatus();
  }

  public Future<?> generateQuads(String dataSet, RdfCreator generator)
      throws LogStorageFailedException, LogProcessingFailedException, IOException {

    DataStores dataStores = dataStoreFactory.getDataStores(dataSet);
    LogStorage logStorage = dataStores.getLogStorage();

    logStorage.startOrContinueAppendLog(generator);
    return scheduleLogSync(dataSet);
  }

  private static class MultiQuadHandler implements QuadHandler, QuadLoader {
    private List<QuadHandler> savers = new ArrayList<>();
    private ThroughputLogger throughputLogger;

    private MultiQuadHandler(ThroughputLogger throughputLogger) {
      this.throughputLogger = throughputLogger;
    }

    public boolean hasSubscriptions() {
      return !savers.isEmpty();
    }

    @Override
    public void start(long lineCount) throws LogProcessingFailedException {
      throughputLogger.started();
      for (QuadHandler saver : savers) {
        saver.start(lineCount);
      }
    }

    @Override
    public void onPrefix(long line, String prefix, String iri) throws LogProcessingFailedException {
      for (QuadHandler saver : savers) {
        saver.onPrefix(line, prefix, iri);
      }
    }

    @Override
    public void onRelation(long line, String subject, String predicate, String object, String graph)
        throws LogProcessingFailedException {
      throughputLogger.tripleProcessed();
      for (QuadHandler saver : savers) {
        saver.onRelation(line, subject, predicate, object, graph);
      }
    }

    @Override
    public void onLiteral(long line, String subject, String predicate, String object, String valueType, String graph)
        throws LogProcessingFailedException {
      throughputLogger.tripleProcessed();
      for (QuadHandler saver : savers) {
        saver.onLiteral(line, subject, predicate, object, valueType, graph);
      }
    }

    @Override
    public void onLanguageTaggedString(long line, String subject, String predicate, String value, String language,
                                       String graph)
        throws LogProcessingFailedException {
      throughputLogger.tripleProcessed();
      for (QuadHandler saver : savers) {
        saver.onLanguageTaggedString(line, subject, predicate, value, language, graph);
      }
    }

    @Override
    public void cancel() throws LogProcessingFailedException {
      for (QuadHandler saver : savers) {
        saver.cancel();
      }
    }

    @Override
    public void finish() throws LogProcessingFailedException {
      throughputLogger.finished();
      for (QuadHandler saver : savers) {
        saver.finish();
      }
    }

    @Override
    public void sendQuads(QuadHandler handler) throws LogProcessingFailedException {
      this.savers.add(handler);
    }
  }
}
