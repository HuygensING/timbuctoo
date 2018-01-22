package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.util.FileHelpers;
import nl.knaw.huygens.timbuctoo.util.LambdaOriginatedException;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.RdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceList;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.FileSystemFileStorage;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.ConcurrentUpdateException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j.Rdf4jIoFactory;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class ImportManagerTest {

  protected File logListLocation;
  protected ImportManager importManager;
  protected File filesDir;
  protected FileSystemFileStorage fileStorage;
  private ResourceList resourceList;

  @Before
  public void makeSimpleDataSet() throws IOException, DataStoreCreationException {
    logListLocation = File.createTempFile("logList", ".json");
    logListLocation.delete();
    filesDir = Files.createTempDir();
    resourceList = mock(ResourceList.class);
    fileStorage = new FileSystemFileStorage(filesDir);
    this.importManager = new ImportManager(
      logListLocation,
      fileStorage,
      fileStorage,
      fileStorage,
      Executors.newSingleThreadExecutor(),
      new Rdf4jIoFactory(),
      resourceList,
      () -> { }
    );
  }

  @After
  public void cleanUp() throws IOException {
    logListLocation.delete();
    FileUtils.cleanDirectory(filesDir);
  }

  @Test
  public void addLogSavesTheLogToDisk() throws Exception {
    File file = FileHelpers.getFileFromResource(ImportManagerTest.class, "clusius.ttl").toFile();
    String name = "http://example.com/clusius.ttl";
    String defaultGraph = "http://example.com/defaultGraph";
    String baseUri = "http://example.com/baseUri";
    Future<ImportStatusReport> promise = importManager.addLog(
      baseUri,
      defaultGraph,
      name,
      new FileInputStream(file),
      Optional.of(Charsets.UTF_8),
      MediaType.valueOf("text/turtle")
    );

    ImportStatusReport statusReport = promise.get();
    assertThat(statusReport.getErrorCount(), is((0)));

    LogEntry logEntry = importManager.getLogEntries().get(0);
    assertThat(logEntry.getBaseUri(), is(baseUri));
    assertThat(logEntry.getDefaultGraph(), is(defaultGraph));
    //The first character is an @. if we can read that we apparently can access the file
    assertThat(fileStorage.getLog(logEntry.getLogToken().get()).getReader().read(), is(64));
  }

  @Test
  public void addLogsCallsTheResourceSyncResourceList() throws Exception {
    String name = "http://example.com/clusius.ttl";
    String defaultGraph = "http://example.com/defaultGraph";
    String baseUri = "http://example.com/baseUri";
    File file = FileHelpers.getFileFromResource(ImportManagerTest.class, "clusius.ttl").toFile();

    Future<ImportStatusReport> promise = importManager.addLog(
      baseUri,
      defaultGraph,
      name,
      new FileInputStream(file),
      Optional.of(Charsets.UTF_8),
      MediaType.valueOf("text/turtle")
    );

    promise.get();

    LogEntry logEntry = importManager.getLogEntries().get(0);
    CachedFile cachedFile = fileStorage.getFile(logEntry.getLogToken().get());
    verify(resourceList).addFile(argThat(CachedFileMatcher.cachedFile(cachedFile)));
  }

  @Test
  public void callsStoresWhenANewLogIsAdded() throws Exception {
    File file = FileHelpers.getFileFromResource(ImportManagerTest.class, "clusius.ttl").toFile();
    String name = "http://example.com/clusius.ttl";
    String defaultGraph = "http://example.com/defaultGraph";
    String baseUri = "http://example.com/baseUri";
    CountingProcessor processor = new CountingProcessor();
    importManager.subscribeToRdf(processor);


    Future<ImportStatusReport> promise = importManager.addLog(
      baseUri,
      defaultGraph,
      name,
      new FileInputStream(file),
      Optional.of(Charsets.UTF_8),
      MediaType.valueOf("text/turtle")
    );
    ImportStatusReport statusReport = promise.get();
    assertThat(processor.getCounter(), is(28));
    assertThat(statusReport.hasErrors(), is(false));
  }

  @Test
  public void generateLogSavesTheLogAndCallsTheStores() throws Exception {
    String defaultGraph = "http://example.com/defaultGraph";
    String baseUri = "http://example.com/baseUri";
    CountingProcessor processor = new CountingProcessor();
    importManager.subscribeToRdf(processor);

    Future<ImportStatusReport> promise = importManager.generateLog(
      baseUri,
      defaultGraph,
      DummyRdfCreator::new
    );

    ImportStatusReport statusReport = promise.get();
    assertThat(statusReport.hasErrors(), is(false));
    assertThat(processor.getCounter(), is(3));
    LogEntry logEntry = importManager.getLogEntries().get(0);
    assertThat(logEntry.getBaseUri(), is(baseUri));
    assertThat(logEntry.getDefaultGraph(), is(defaultGraph));
    //The first character is an < (start of a uri in nquads) if we can read that we apparently can access the file
    assertThat(fileStorage.getLog(logEntry.getLogToken().get()).getReader().read(), is(60));
  }

  @Test
  public void addFileCallsTheResourceSyncResourceList() throws Exception {
    String name = "http://example.com/clusius.ttl";
    File file = FileHelpers.getFileFromResource(ImportManagerTest.class, "clusius.ttl").toFile();

    String fileToken = importManager.addFile(
      new FileInputStream(file),
      name.toString(),
      MediaType.valueOf("text/turtle")
    );

    verify(resourceList).addFile(argThat(CachedFileMatcher.cachedFile(fileStorage.getFile(fileToken))));
  }

  private static class CachedFileMatcher extends CompositeMatcher<CachedFile> {
    private CachedFileMatcher() {

    }

    public static CachedFileMatcher cachedFile() {
      return new CachedFileMatcher();
    }

    private static CachedFileMatcher cachedFile(CachedFile cachedFile) {
      return cachedFile()
        .withFile(cachedFile.getFile())
        .withMimeType(cachedFile.getMimeType())
        .withName(cachedFile.getName());
    }

    public CachedFileMatcher withFile(File file) {
      this.addMatcher(new PropertyEqualityMatcher<CachedFile, File>("file", file) {
        @Override
        protected File getItemValue(CachedFile item) {
          return item.getFile();
        }
      });
      return this;
    }

    public CachedFileMatcher withName(String name) {
      this.addMatcher(new PropertyEqualityMatcher<CachedFile, String>("name", name) {
        @Override
        protected String getItemValue(CachedFile item) {
          return item.getName();
        }
      });
      return this;
    }

    public CachedFileMatcher withMimeType(MediaType mimeType) {
      this.addMatcher(new PropertyEqualityMatcher<CachedFile, MediaType>("mimeType", mimeType) {
        @Override
        protected MediaType getItemValue(CachedFile item) {
          return item.getMimeType();
        }
      });
      return this;
    }


  }

  private static class CountingProcessor implements RdfProcessor {
    private final AtomicInteger counter;
    private int currentVersion = -1;

    public CountingProcessor() {
      counter = new AtomicInteger();
    }

    private int getCounter() {
      return counter.get();
    }


    @Override
    public void setPrefix(String prefix, String iri) throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void addRelation(String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void addValue(String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void addLanguageTaggedString(String subject, String predicate, String value,
                                        String language,
                                        String graph) throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void delRelation(String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void delValue(String subject, String predicate, String value, String valueType,
                         String graph)
      throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void delLanguageTaggedString(String subject, String predicate, String value,
                                        String language,
                                        String graph) throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }

    @Override
    public void start(int index) throws RdfProcessingFailedException {
      currentVersion = index;
      counter.incrementAndGet();
    }

    @Override
    public int getCurrentVersion() {
      return currentVersion;
    }

    @Override
    public void commit() throws RdfProcessingFailedException {
      counter.incrementAndGet();
    }
  }

  @Test
  public void lambdaOriginatedExceptionsAreCaught() throws Exception {
    String defaultGraph = "http://example.com/defaultGraph";
    String baseUri = "http://example.com/baseUri";
    String errorMsg = "OMG, concurrent update!";
    final ConcurrentUpdateException cue = new ConcurrentUpdateException(errorMsg);
    Supplier<RdfCreator> supplier = () -> {
      throw new LambdaOriginatedException(cue);
    };
    Future<ImportStatusReport> promise = importManager.generateLog(baseUri, defaultGraph, supplier);
    ImportStatusReport statusReport = promise.get();
    assertThat(statusReport.getLastError(), is(cue));
    assertThat(statusReport.getMessages().size(), is(3));
    assertThat(statusReport.getMessages().get(1).contains(errorMsg), is(true));
    assertThat(statusReport.getStatus(), is("Finished import with 1 error"));
  }

  @Test
  public void multipleUpdatesAreSerialized() throws  Exception {
    long wait = 10;
    int threads = 10;
    final CountingProcessor processor = new CountingProcessor();
    importManager.subscribeToRdf(processor);
    String defaultGraph = "http://example.com/defaultGraph";
    List<Callable<ImportStatusReport>> tasks = new ArrayList<>();

    tasks.add(() -> importManager.generateLog("foo", "bar", () -> {
      //System.out.println("I throw foo");
      throw new LambdaOriginatedException(new Exception("foo"));
    }).get());

    for (int i = 0; i < threads; i ++) {
      final int[] in = {i};
      String baseUri = "http://example.com/baseUri/" + i;
      Supplier<RdfCreator> supplier = () -> (PatchRdfCreator) saver -> {
        try {
          //System.out.println(processor.counter + " I am supplier " + in[0] + ". I wait " + wait + " msecs.");
          Thread.sleep(wait);
        } catch (InterruptedException e) {
          throw new IllegalStateException(e);
        }
      };
      tasks.add(() -> importManager.generateLog(baseUri, defaultGraph, supplier).get());
    }

    tasks.add(() -> importManager.generateLog("footoo", "bartoo", () -> {
      //System.out.println("I throw bar");
      throw new LambdaOriginatedException(new Exception("bar"));
    }).get());

    //Collections.shuffle(tasks);

    ExecutorService executor = Executors.newCachedThreadPool();
    List<ImportStatusReport> statusReports = executor.invokeAll(tasks)
      .stream()
      .map(future -> {
        try {
          return future.get();
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      })
      .collect(Collectors.toList());

    executor.shutdown();
    assertThat(processor.counter.get(), is(2 * threads));
  }

  @Test
  @Ignore("Endless test")
  public void keepTesting() throws Exception {
    // Stops after round ~92:
    // java.io.FileNotFoundException:
    // /var/folders/4r/07w38gh12d395r8s9n0pw30w0000gn/T/logList2627116416061327169.json (Too many open files)
    int counter = 0;
    while (1 == 1) {
      multipleUpdatesAreSerialized();
      System.out.println(counter++);
      Thread.sleep(1);
    }
  }


}
