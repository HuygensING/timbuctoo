package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.v5.filestorage.LogStorage;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.FileStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfParser;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import org.slf4j.Logger;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * - provides methods for adding data (files and rdf). Will persist that data
 * - makes sure that the derived stores are kept in sync
 */
public class DataSet {
  private final FileStorage fileStorage;
  private final FileStorage imageStorage;
  private final LogStorage logStorage;
  private final RdfIoFactory serializerFactory;
  private final ExecutorService executorService;
  private final RdfParser rdfParser;
  private static final Logger LOG = getLogger(DataSet.class);
  private final DataStores dataStores;
  private final JsonFileBackedData<LogList> logListStore;

  public DataSet(File logListLocation, FileStorage fileStorage, FileStorage imageStorage, LogStorage logStorage,
                 DataStores dataStores, ExecutorService executorService,
                 RdfIoFactory rdfIoFactory)
      throws DataStoreCreationException {
    this.fileStorage = fileStorage;
    this.imageStorage = imageStorage;
    this.logStorage = logStorage;
    this.serializerFactory = rdfIoFactory;
    this.executorService = executorService;
    this.rdfParser = rdfIoFactory.makeRdfParser();
    this.dataStores = dataStores;
    try {
      logListStore = JsonFileBackedData.getOrCreate(
        logListLocation,
        new LogList(),
        new TypeReference<LogList>() {}
      );
    } catch (IOException e) {
      throw new DataStoreCreationException(e);
    }
  }

  public Future<?> addLog(URI name, InputStream rdfInputStream, Optional<Charset> charset,
                          Optional<MediaType> mediaType) throws LogStorageFailedException {
    try {
      String token = logStorage.saveLog(rdfInputStream, name.toString(), mediaType, charset);
      int[] index = new int[1];
      logListStore.updateData(logList -> {
        index[0] = logList.addEntry(LogEntry.create(name, token));
        return logList;
      });
      return executorService.submit(() -> processLogsUntil(index[0]));
    } catch (IOException e) {
      throw new LogStorageFailedException(e);
    }
  }

  public String addFile(InputStream fileStream, String fileName, Optional<MediaType> mediaType)
      throws FileStorageFailedException {
    try {
      return fileStorage.saveFile(fileStream, fileName, mediaType);
    } catch (IOException e) {
      throw new FileStorageFailedException(e);
    }
  }

  public String addImage(InputStream imageStream, String imageName, Optional<MediaType> mediaType)
      throws FileStorageFailedException {
    try {
      return imageStorage.saveFile(imageStream, imageName, mediaType);
    } catch (IOException e) {
      throw new FileStorageFailedException(e);
    }
  }

  public Future<?> generateLog(URI name, RdfCreator creator) throws LogStorageFailedException {
    try {
      //add to the log structure
      int[] index = new int[1];
      logListStore.updateData(logList -> {
        index[0] = logList.addEntry(LogEntry.create(name, creator));
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
    while (unprocessed.hasNext() && unprocessed.nextIndex() < maxIndex) {
      int index = unprocessed.nextIndex();
      LogEntry entry = unprocessed.next();
      if (entry.getLogToken().isPresent()) {
        try {
          CachedLog log = logStorage.getLog(entry.getLogToken().get());
          processLogIfNeeded(index, log, dataStores.getTripleStore());
          processLogIfNeeded(index, log, dataStores.getCollectionIndex());
          logListStore.updateData(logList -> {
            logList.markAsProcessed(index);
            return logList;
          });
        } catch (RdfProcessingFailedException | IOException e) {
          LOG.error("Processing the log failed", e);
          break;
        }
      } else {
        RdfCreator creator = entry.getRdfCreator().get();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        String token = "";
        try (RdfSerializer serializer = serializerFactory.makeRdfSerializer(outputStream)) {
          creator.sendQuads(serializer);
          token = logStorage.saveLog(
            new ByteArrayInputStream(outputStream.toByteArray()),
            "",
            Optional.of(serializer.getMediaType()),
            Optional.of(serializer.getCharset())
          );
          LogEntry entryWithLog = LogEntry.addLogToEntry(entry, token);
          unprocessed.set(entryWithLog);

          token = "";
          unprocessed.previous(); //move back to process this item again
        } catch (IOException | LogStorageFailedException e) {
          if (token.isEmpty()) {
            LOG.error("Log processing failed", e);
          } else {
            LOG.error("Log processing failed. Log created but not added to the list!", e);
          }
          break;
        }
      }
    }
  }

  private void processLogIfNeeded(int index, CachedLog log, RdfProcessor processor)
      throws RdfProcessingFailedException {
    String currentCursor = processor.getStatus();
    if (currentCursor == null) {
      currentCursor = "";
    }
    String[] cursorParts = currentCursor.split("\n", 2);
    int major = Integer.parseInt(cursorParts[0]);
    if (major < index) {
      rdfParser.importRdf(index + "\n", "", log, processor);
    } else if (major == index) {
      rdfParser.importRdf(index + "\n", cursorParts[1], log, processor);
    }
  }

  public DataStores getDataStores() {
    return dataStores;
  }
}
