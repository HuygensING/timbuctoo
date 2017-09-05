package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Stopwatch;
import nl.knaw.huygens.timbuctoo.util.Tuple;
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
  private final List<Tuple<Integer, RdfProcessor>> subscribedProcessors;
  private final List<Tuple<Integer, EntityProcessor>> subscribedEntityProcessors;
  private final Runnable webhooks;
  private EntityProvider entityProvider;

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
    subscribedEntityProcessors = new ArrayList<>();
  }

  public Future<?> addLog(String baseUri, String defaultGraph, String fileName, InputStream rdfInputStream,
                          Optional<Charset> charset, MediaType mediaType) throws LogStorageFailedException {
    try {
      String token = logStorage.saveLog(rdfInputStream, fileName, mediaType, charset);
      int[] index = new int[1];
      logListStore.updateData(logList -> {
        index[0] = logList.addEntry(LogEntry.create(baseUri, defaultGraph, token));
        return logList;
      });
      return executorService.submit(() -> processLogsUntil(index[0]));
    } catch (IOException e) {
      throw new LogStorageFailedException(e);
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

  public Future<?> generateLog(String baseUri, String defaultGraph, RdfCreator creator)
    throws LogStorageFailedException {
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
      throw new LogStorageFailedException(e);
    }
  }

  public Future<?> processLogs() {
    return executorService.submit(() -> processLogsUntil(Integer.MAX_VALUE));
  }

  private void processLogsUntil(int maxIndex) {
    ListIterator<LogEntry> unprocessed = logListStore.getData().getUnprocessed();
    boolean dataWasAdded = false;
    while (unprocessed.hasNext() && unprocessed.nextIndex() <= maxIndex) {
      int index = unprocessed.nextIndex();
      LogEntry entry = unprocessed.next();
      if (entry.getLogToken().isPresent()) {
        try {
          CachedLog log = logStorage.getLog(entry.getLogToken().get());
          final Stopwatch stopwatch = Stopwatch.createStarted();
          for (Tuple<Integer, RdfProcessor> subscribedProcessor : subscribedProcessors) {
            if (subscribedProcessor.getLeft() <= index) {
              RdfProcessor processor = subscribedProcessor.getRight();
              RdfParser rdfParser = serializerFactory.makeRdfParser(log);
              LOG.info("******* " + processor.getClass().getSimpleName() + " Started importing full log...");
              processor.start(index);
              rdfParser.importRdf(log, entry.getBaseUri(), entry.getDefaultGraph(), processor);
              processor.commit();
            }
          }
          for (Tuple<Integer, EntityProcessor> processor : subscribedEntityProcessors) {
            entityProvider.processEntities(processor.getLeft(), processor.getRight());
          }

          LOG.info("Finished importing. Total import took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds.");
          logListStore.updateData(logList -> {
            logList.markAsProcessed(index);
            return logList;
          });
          dataWasAdded = true;
        } catch (RdfProcessingFailedException | IOException e) {
          LOG.error("Processing the log failed", e);
          break;
        }
      } else {
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
            break;
          }
        } else {
          try (RdfPatchSerializer srlzr = serializerFactory.makeRdfPatchSerializer(outputStream, entry.getBaseUri())) {
            mediaType = srlzr.getMediaType();
            charset = Optional.of(srlzr.getCharset());
            ((PatchRdfCreator) creator).sendQuads(srlzr);
          } catch (Exception e) {
            LOG.error("Log generation failed", e);
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
          break;
        }
      }
    }
    if (dataWasAdded) {
      webhooks.run();
    }
  }

  @Override
  public void subscribeToRdf(RdfProcessor processor, int cursor) {
    subscribedProcessors.add(Tuple.tuple(cursor, processor));
    if (processor instanceof EntityProvider) {
      entityProvider = (EntityProvider) processor;
    }
  }

  @Override
  public void subscribeToEntities(EntityProcessor processor, int cursor) {
    subscribedEntityProcessors.add(Tuple.tuple(cursor, processor));
  }

  public boolean isRdfTypeSupported(MediaType mediaType) {
    return serializerFactory.isRdfTypeSupported(mediaType);
  }

  List<LogEntry> getLogEntries() {
    return logListStore.getData().getEntries();
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
