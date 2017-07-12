package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.FileSystemFileStorage;
import nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j.Rdf4jIoFactory;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.io.Resources.getResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ImportManagerTest {

  protected File logListLocation;
  protected ImportManager importManager;
  protected File filesDir;
  protected FileSystemFileStorage fileStorage;

  @Before
  public void makeSimpleDataSet() throws IOException, DataStoreCreationException {
    logListLocation = File.createTempFile("logList", ".json");
    logListLocation.delete();
    filesDir = Files.createTempDir();
    fileStorage = new FileSystemFileStorage(filesDir);
    this.importManager = new ImportManager(
      logListLocation,
      fileStorage,
      fileStorage,
      fileStorage,
      Executors.newSingleThreadExecutor(),
      new Rdf4jIoFactory()
    );
  }

  @After
  public void cleanUp() throws IOException {
    logListLocation.delete();
    FileUtils.cleanDirectory(filesDir);
  }

  @Test
  public void addLogSavesTheLogToDisk() throws Exception {
    File file = new File(getResource(ImportManagerTest.class, "clusius.ttl").toURI());
    URI name = URI.create("http://example.com/clusius.ttl");
    importManager.addLog(
      name,
      new FileInputStream(file),
      Optional.of(Charsets.UTF_8),
      Optional.of(MediaType.valueOf("text/turtle"))
    );

    LogEntry logEntry = importManager.getLogEntries().get(0);
    assertThat(logEntry.getName(), is(name));
    //The first character is an @. if we can read that we apparently can access the file
    assertThat(fileStorage.getLog(logEntry.getLogToken().get()).getReader().read(), is(64));
  }

  @Test
  public void callsStoresWhenANewLogIsAdded() throws Exception {
    File file = new File(getResource(ImportManagerTest.class, "clusius.ttl").toURI());
    URI name = URI.create("http://example.com/clusius.ttl");
    CountingProcessor processor = new CountingProcessor();
    importManager.subscribeToRdf(processor, null);


    Future<?> promise = importManager.addLog(
      name,
      new FileInputStream(file),
      Optional.of(Charsets.UTF_8),
      Optional.of(MediaType.valueOf("text/turtle"))
    );
    promise.get();
    assertThat(processor.getCounter(), is(28));
  }

  @Test
  public void generateLogSavesTheLogAndCallsTheStores() throws Exception {
    URI name = URI.create("http://example.com/clusius.ttl");
    CountingProcessor processor = new CountingProcessor();
    importManager.subscribeToRdf(processor, null);

    Future<?> promise = importManager.generateLog(
      name,
      new DummyRdfCreator()
    );

    promise.get();
    assertThat(processor.getCounter(), is(3));
    LogEntry logEntry = importManager.getLogEntries().get(0);
    assertThat(logEntry.getName(), is(name));
    //The first character is an < (start of a uri in nquads) if we can read that we apparently can access the file
    assertThat(fileStorage.getLog(logEntry.getLogToken().get()).getReader().read(), is(60));
  }


  private static class CountingProcessor implements RdfProcessor {
    private final AtomicInteger counter;

    public CountingProcessor() {
      counter = new AtomicInteger();
    }

    private int getCounter() {
      return counter.get();
    }


    @Override
    public void setPrefix(String cursor, String prefix, String iri) throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void addRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void addValue(String cursor, String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void addLanguageTaggedString(String cursor, String subject, String predicate, String value,
                                        String language,
                                        String graph) throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void delRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void delValue(String cursor, String subject, String predicate, String value, String valueType,
                         String graph)
      throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void delLanguageTaggedString(String cursor, String subject, String predicate, String value,
                                        String language,
                                        String graph) throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void start() throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void finish() throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }
  }

}
