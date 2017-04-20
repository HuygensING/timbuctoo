package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import com.sleepycat.je.DatabaseException;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore.LogMetadata;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore.LogStorage;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.dto.LocalLog;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfreader.RdfParser;

public class ImportManager {

  final LogMetadata metadata;
  private final LogStorage logStorage;
  private final RdfParser rdfParser;
  private final DataStoreFactory dataStoreFactory;

  public ImportManager(
    LogMetadata metadata,
    LogStorage logStorage,
    RdfParser rdfParser,
    DataStoreFactory dataStoreFactory
  ) {
    this.metadata = metadata;
    this.logStorage = logStorage;
    this.rdfParser = rdfParser;
    this.dataStoreFactory = dataStoreFactory;
  }

  public void addLog(String dataSet, LocalLog log) throws LogProcessingFailedException {
    metadata.addLog(dataSet, log.getName());
    importLog(dataSet, log);
  }

  public void generateQuads(String dataSet, QuadGenerator generator)
      throws LogStorageFailedException, LogProcessingFailedException {

    LocalLog log = metadata.startOrContinueAppendLog(dataSet);
    QuadHandler logWriter = logStorage.startWritingToLog(log);
    generator.sendQuads(logWriter);
    metadata.appendToLogFinished(dataSet);
    importLog(dataSet, log);
  }

  private void importLog(String dataSet, LocalLog log) throws LogProcessingFailedException {
    try {
      DataStores dataStores = dataStoreFactory.getDataStores(dataSet);
      TripleStore tripleStore = dataStores.getTripleStore();
      CollectionIndex collectionIndex = dataStores.getCollectionIndex();

      log.loadQuads(rdfParser, new QuadHandler() {
        @Override
        public void start() throws LogProcessingFailedException {
          tripleStore.start();
          collectionIndex.start();
        }

        @Override
        public void onPrefix(String prefix, String iri) throws LogProcessingFailedException {
          tripleStore.onPrefix(prefix, iri);
          collectionIndex.onPrefix(prefix, iri);
        }

        @Override
        public void onQuad(String subject, String predicate, String object, String valueType, String graph)
            throws LogProcessingFailedException {
          tripleStore.onQuad(subject, predicate, object, valueType, graph);
          collectionIndex.onQuad(subject, predicate, object, valueType, graph);
        }

        @Override
        public void finish() throws LogProcessingFailedException {
          tripleStore.finish();
          collectionIndex.finish();
        }
      });
    } catch (DatabaseException e) {
      throw new LogProcessingFailedException(e);
    }
  }
}
