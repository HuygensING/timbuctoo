package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Stopwatch;
import nl.knaw.huygens.timbuctoo.util.LambdaOriginatedException;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.RdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceList;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.v5.filestorage.LogStorage;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.FileStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfParser;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.slf4j.Logger;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * - provides methods for adding data (files and rdf). Will persist that data
 * - makes sure that the derived stores are kept in sync
 */
public class ImportManager implements DataProvider {
  private static final Logger LOG = getLogger(ImportManager.class);
  private final FileStorage fileStorage;
  private final FileStorage imageStorage;
  private final LogStorage logStorage;
  private final RdfIoFactory serializerFactory;
  private final ExecutorService executorService;
  private final JsonFileBackedData<LogList> logListStore;
  private final List<RdfProcessor> subscribedProcessors;
  private final Runnable webhooks;
  private final ImportStatus importStatus;
  private final DataSetImportStatus dataSetImportStatus;
  private final Object logMonitor;

  public ImportManager(File logListLocation, FileStorage fileStorage, FileStorage imageStorage, LogStorage logStorage,
                       ExecutorService executorService, RdfIoFactory rdfIoFactory, ResourceList resourceList,
                       Runnable onUpdated)
    throws DataStoreCreationException {
    this.webhooks = onUpdated;
    this.fileStorage = new PublicFileStore(fileStorage, resourceList);
    this.imageStorage = new PublicFileStore(imageStorage, resourceList);
    this.logStorage = new PublicLogStore(logStorage, resourceList);
    this.serializerFactory = rdfIoFactory;
    this.executorService = executorService;
    try {
      logListStore = JsonFileBackedData.getOrCreate(
        logListLocation,
        LogList::new,
        new TypeReference<LogList>() {
        }
      );
    } catch (IOException e) {
      throw new DataStoreCreationException(e);
    }
    subscribedProcessors = new ArrayList<>();
    importStatus = new ImportStatus(logListStore.getData());
    dataSetImportStatus = new DataSetImportStatus(logListStore.getData());
    logMonitor = new Object();
  }

  public Future<ImportStatus> addLog(String baseUri, String defaultGraph, String fileName,
                                                   InputStream rdfInputStream,
                                                   Optional<Charset> charset, MediaType mediaType)
    throws LogStorageFailedException {

    synchronized (logMonitor) {
      importStatus.start(this.getClass().getSimpleName() + ".addLog", baseUri);
      try {
        int[] index = new int[1];
        String token = logStorage.saveLog(rdfInputStream, fileName, mediaType, charset);
        logListStore.updateData(logList -> {
          index[0] = logList.addEntry(LogEntry.create(baseUri, defaultGraph, token));
          return logList;
        });
        return executorService.submit(() -> processLogsUntil(index[0]));
      } catch (IOException e) {
        importStatus.addError("Could not save log", e);
        throw new LogStorageFailedException(e);
      }
    }
  }

  public String addFile(InputStream fileStream, String fileName, MediaType mediaType)
    throws FileStorageFailedException {
    try {
      return fileStorage.saveFile(fileStream, fileName, mediaType);
    } catch (IOException e) {
      throw new FileStorageFailedException(e);
    }
  }

  public CachedFile getFile(String fileToken) throws IOException {
    return fileStorage.getFile(fileToken);
  }

  public String addImage(InputStream imageStream, String imageName, MediaType mediaType)
    throws FileStorageFailedException {
    try {
      return imageStorage.saveFile(imageStream, imageName, mediaType);
    } catch (IOException e) {
      throw new FileStorageFailedException(e);
    }
  }

  // @ToDo replace all calls to this method to the one that takes a Supplier<RdfCreator>
  public Future<ImportStatus> generateLog(String baseUri, String defaultGraph, RdfCreator creator)
    throws LogStorageFailedException {

    importStatus.start(this.getClass().getSimpleName() + ".generateLog", baseUri);
    try {
      //add to the log structure
      int[] index = new int[1];
      logListStore.updateData(logList -> {
        index[0] = logList.addEntry(LogEntry.create(baseUri, defaultGraph, creator));
        return logList;
      });
      //schedule processing
      return executorService.submit(() -> processLogsUntil(index[0]));
    } catch (IOException e) {
      importStatus.addError("Could not update logList", e);
      throw new LogStorageFailedException(e);
    }
  }

  public Future<ImportStatus> generateLog(String baseUri, String defaultGraph,
                                                       Supplier<RdfCreator> supplier)
    throws LogStorageFailedException {

    synchronized (logMonitor) {
      importStatus.start(this.getClass().getSimpleName() + ".generateLog", baseUri);
      try {
        //add to the log structure
        int[] index = new int[1];
        logListStore.updateData(logList -> {
          index[0] = logList.addEntry(LogEntry.create(baseUri, defaultGraph, supplier.get()));
          return logList;
        });
        //schedule processing
        return executorService.submit(() -> processLogsUntil(index[0]));
      } catch (IOException e) {
        importStatus.addError("Could not update logList", e);
        throw new LogStorageFailedException(e);
      } catch (LambdaOriginatedException e) {
        importStatus.addError("Could not supply RdfCreator", e.getCause());
        endImport();
        return ConcurrentUtils.constantFuture(importStatus);
      }
    }
  }

  public Future<ImportStatus> processLogs() {
    synchronized (logMonitor) {
      importStatus.start(this.getClass().getSimpleName() + ".processLogs", null);
      return executorService.submit(() -> processLogsUntil(Integer.MAX_VALUE));
    }
  }

  // All calls to this method should be synchronized on logMonitor
  private ImportStatus processLogsUntil(int maxIndex) {
    ListIterator<LogEntry> unprocessed = logListStore.getData().getUnprocessed();
    boolean dataWasAdded = false;
    while (unprocessed.hasNext() && unprocessed.nextIndex() <= maxIndex) {
      int index = unprocessed.nextIndex();
      LogEntry entry = unprocessed.next();
      importStatus.startEntry(entry);
      if (entry.getLogToken().isPresent()) { // logToken
        String logToken = entry.getLogToken().get();
        try {
          CachedLog log = logStorage.getLog(logToken);
          final Stopwatch stopwatch = Stopwatch.createStarted();
          for (RdfProcessor processor : subscribedProcessors) {
            if (processor.getCurrentVersion() <= index) {
              String msg = "******* " + processor.getClass().getSimpleName() + " Started importing full log...";
              LOG.info(msg);
              importStatus.setStatus(msg);
              RdfParser rdfParser = serializerFactory.makeRdfParser(log);
              processor.start(index);
              rdfParser.importRdf(log, entry.getBaseUri(), entry.getDefaultGraph(), processor);
              processor.commit();
            }
          }
          long elapsedTime = stopwatch.elapsed(TimeUnit.SECONDS);
          String msg = "Finished importing. Total import took " + elapsedTime + " seconds.";
          LOG.info(msg);
          importStatus.setStatus(msg);
          dataWasAdded = true;
        } catch (RdfProcessingFailedException | IOException e) {
          LOG.error("Processing log failed", e);
          importStatus.addError("Processing log failed", e);
        }

        // Update the log, even after RdfProcessingFailedException | IOException
        try {
          logListStore.updateData(logList -> {
            logList.markAsProcessed(index);
            return logList;
          });
        } catch (IOException e) {
          LOG.error("Updating the log failed", e);
          importStatus.addError("Updating log failed", e);
        }
      } else { // no logToken
        RdfCreator creator = entry.getRdfCreator().get();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();//FIXME: write to tempFile

        String token = "";
        MediaType mediaType;
        Optional<Charset> charset;

        if (creator instanceof PlainRdfCreator) {
          try (RdfSerializer serializer = serializerFactory.makeRdfSerializer(outputStream)) {
            mediaType = serializer.getMediaType();
            charset = Optional.of(serializer.getCharset());
            ((PlainRdfCreator) creator).sendQuads(serializer);
          } catch (Exception e) {
            LOG.error("Log generation failed", e);
            importStatus.addError("Log generation failed", e);
            break;
          }
        } else {
          try (RdfPatchSerializer srlzr = serializerFactory.makeRdfPatchSerializer(outputStream, entry.getBaseUri())) {
            mediaType = srlzr.getMediaType();
            charset = Optional.of(srlzr.getCharset());
            ((PatchRdfCreator) creator).sendQuads(srlzr);
          } catch (Exception e) {
            LOG.error("Log generation failed", e);
            importStatus.addError("Log generation failed", e);
            break;
          }
        }

        try {
          token = logStorage.saveLog(
            new ByteArrayInputStream(outputStream.toByteArray()),
            "log_generated_by_" + creator.getClass().getSimpleName(),
            mediaType,
            charset
          );
          LogEntry entryWithLog = LogEntry.addLogToEntry(entry, token);
          unprocessed.set(entryWithLog);

          token = "";
          unprocessed.previous(); //move back to process this item again
        } catch (Exception e) {
          if (token.isEmpty()) {
            LOG.error("Log processing failed", e);
          } else {
            LOG.error("Log processing failed. Log created but not added to the list!", e);
          }
          importStatus.addError("Log processing failed", e);
          break;
        }
      } // end else with no condition
      importStatus.finishEntry();
    } // end main while loop
    if (dataWasAdded) {
      webhooks.run();
    }
    endImport();
    return importStatus;
  }

  private void endImport() {
    importStatus.finishList();
    // update log.json
    try {
      logListStore.updateData(Function.identity());
    } catch (IOException e) {
      LOG.error("Updating the log failed", e);
      importStatus.addError("Updating log failed", e);
    }
  }


  @Override
  public void subscribeToRdf(RdfProcessor processor) {
    subscribedProcessors.add(processor);
  }

  public boolean isRdfTypeSupported(MediaType mediaType) {
    return serializerFactory.isRdfTypeSupported(mediaType);
  }

  List<LogEntry> getLogEntries() {
    return logListStore.getData().getEntries();
  }

  public ImportStatus getImportStatus() {
    return importStatus;
  }

  public DataSetImportStatus getDataSetImportStatus() {
    return dataSetImportStatus;
  }

  // wrapper class that makes sure all files are exposed by resource sync
  private static class PublicFileStore implements FileStorage {
    private final FileStorage fileStorage;
    private final ResourceList resourceList;

    public PublicFileStore(FileStorage fileStorage, ResourceList resourceList) {
      this.fileStorage = fileStorage;
      this.resourceList = resourceList;
    }

    @Override
    public String saveFile(InputStream stream, String fileName, MediaType mediaType) throws IOException {
      String token = fileStorage.saveFile(stream, fileName, mediaType);
      try {
        resourceList.addFile(getFile(token));
      } catch (ResourceSyncException e) {
        throw new IOException(e);
      }
      return token;
    }

    @Override
    public CachedFile getFile(String token) throws IOException {
      return fileStorage.getFile(token);
    }
  }

  // wrapper class that makes sure all logs are exposed by resource sync
  private static class PublicLogStore implements LogStorage {
    private final LogStorage logStorage;
    private final ResourceList resourceList;

    public PublicLogStore(LogStorage logStorage, ResourceList resourceList) {
      this.logStorage = logStorage;
      this.resourceList = resourceList;
    }

    @Override
    public String saveLog(InputStream stream, String fileName, MediaType mediaType, Optional<Charset> charset)
      throws IOException {
      String token = logStorage.saveLog(stream, fileName, mediaType, charset);
      try {
        resourceList.addFile(getLog(token));
      } catch (ResourceSyncException e) {
        throw new IOException(e);
      }
      return token;
    }

    @Override
    public CachedLog getLog(String token) throws IOException {
      return logStorage.getLog(token);
    }
  }
}
