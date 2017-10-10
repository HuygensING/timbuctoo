package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.io.Files;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthorizer;
import nl.knaw.huygens.timbuctoo.security.dataaccess.localfile.LocalFileVreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbDataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.NonPersistentBdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DataSetRepositoryTest {

  private DataSetRepository dataSetRepository;
  protected File tempFile;
  private ResourceSync resourceSync;

  @Before
  public void init() throws IOException, InterruptedException {
    tempFile = Files.createTempDir();
    resourceSync = mock(ResourceSync.class);
    dataSetRepository = createDataSetRepo();
  }

  private DataSetRepository createDataSetRepo() throws IOException {
    return new DataSetRepository(
      Executors.newSingleThreadExecutor(),
      new JsonBasedAuthorizer(new LocalFileVreAuthorizationAccess(tempFile.toPath())),
      ImmutableDataSetConfiguration.builder()
                                   .dataSetMetadataLocation(tempFile.getAbsolutePath())
                                   .rdfIo(mock(RdfIoFactory.class, RETURNS_DEEP_STUBS))
                                   .fileStorage(mock(FileStorageFactory.class, RETURNS_DEEP_STUBS))
                                   .resourceSync(resourceSync)
                                   .build(),
      new BdbDataStoreFactory(new NonPersistentBdbDatabaseCreator()),
      new TimbuctooRdfIdHelper("http://example.org/timbuctoo/"),
      combinedId -> { }
    );
  }

  @After
  public void cleanUp() {
    tempFile.delete();
  }

  @Test
  public void createDataSetReturnsTheSamesDataSetForEachCall() throws DataStoreCreationException {
    DataSet dataSet1 = dataSetRepository.createDataSet(User.create(null, "user"), "dataset");
    DataSet dataSet2 = dataSetRepository.createDataSet(User.create(null, "user"), "dataset");

    assertThat(dataSet1, is(sameInstance(dataSet2)));
  }

  public ImportManager getImportManager(String user, String dataset) throws DataStoreCreationException {
    return dataSetRepository.createDataSet(User.create(null, user), dataset).getImportManager();
  }

  @Test
  public void createDataSetReturnsADifferentDataSetForDifferentDataSetIds() throws DataStoreCreationException {
    ImportManager importManager1 = getImportManager("user", "dataset");
    ImportManager importManager2 = getImportManager("user", "other");

    assertThat(importManager1, is(not(sameInstance(importManager2))));
  }

  @Test
  public void createDataSetReturnsADifferentDataSetForDifferentUserIds() throws DataStoreCreationException {
    ImportManager importManager1 = getImportManager("user", "dataset");
    ImportManager importManager2 = getImportManager("other", "dataset");

    assertThat(importManager1, is(not(sameInstance(importManager2))));
  }

  @Test
  public void createImportManagerOnlyAddsANewDataSetToResourceSync() throws Exception {
    dataSetRepository.createDataSet(User.create(null, "user"), "dataset");
    dataSetRepository.createDataSet(User.create(null, "user"), "dataset");

    verify(resourceSync, times(1)).resourceList("uuser", "dataset");
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserIsNotKnown() {
    boolean dataSetExists = dataSetRepository.dataSetExists("ownerId", "dataSetId");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserDoesNotOwnADataSetWithTheDataSetId() throws DataStoreCreationException {
    dataSetRepository.createDataSet(User.create(null, "ownerId"), "otherDataSetId");

    boolean dataSetExists = dataSetRepository.dataSetExists("ownerId", "dataSetId");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void removeDataSetRemovesTheDataSetFromDisk() throws Exception {
    final DataSet dataSet = dataSetRepository.createDataSet(User.create(null, "user"), "dataSet");
    File dataSetPath = new File(new File(tempFile, dataSet.getMetadata().getOwnerId()), "dataSet");
    assertThat(dataSetPath.exists(), is(true));

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataSet");

    assertThat(dataSetPath.exists(), is(false));
  }

  @Test
  public void removeDataSetRemovesTheDataSetFromTheIndex() throws Exception {
    final DataSet dataSet = dataSetRepository.createDataSet(User.create(null, "user"), "dataSet");

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataSet");

    assertThat(dataSetRepository.dataSetExists(dataSet.getMetadata().getOwnerId(), "dataSet"), is(false));
  }

  @Test
  public void removeDataSetRemovesItFromResourceSync() throws Exception {
    final DataSet dataSet = dataSetRepository.createDataSet(User.create(null, "user"), "dataSet");

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataSet");

    verify(resourceSync).removeDataSet(dataSet.getMetadata().getOwnerId(), "dataSet");
  }

  @Test
  public void dataSetsWillBeTheSameAfterRestart() throws Exception {
    final DataSet dataSet = dataSetRepository.createDataSet(User.create(null, "user"), "dataSet");

    assertThat(dataSetRepository.dataSetExists(dataSet.getMetadata().getOwnerId(), "dataSet"), is(true));

    // create a new instance to simulate a restart
    dataSetRepository = createDataSetRepo();
    dataSetRepository.start();

    assertThat(dataSetRepository.dataSetExists(dataSet.getMetadata().getOwnerId(), "dataSet"), is(true));
  }

}
