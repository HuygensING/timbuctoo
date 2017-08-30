package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.io.Files;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthorizer;
import nl.knaw.huygens.timbuctoo.security.dataaccess.localfile.LocalFileVreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbDataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.NonPersistentBdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;
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
    dataSetRepository = new DataSetRepository(
      Executors.newSingleThreadExecutor(),
      new JsonBasedAuthorizer(new LocalFileVreAuthorizationAccess(tempFile.toPath())),
      ImmutableDataSetConfiguration.builder()
                                   .dataSetMetadataLocation(tempFile.getAbsolutePath())
                                   .rdfIo(mock(RdfIoFactory.class, RETURNS_DEEP_STUBS))
                                   .fileStorage(mock(FileStorageFactory.class, RETURNS_DEEP_STUBS))
                                   .resourceSync(resourceSync)
                                   .build(),
      new BdbDataStoreFactory(new NonPersistentBdbDatabaseCreator())
    );
  }

  @After
  public void cleanUp() {
    tempFile.delete();
  }

  @Test
  public void getOrCreateReturnsTheSamesDataSetForEachCall() throws DataStoreCreationException {
    ImportManager importManager1 = getImportManager("user", "dataset");
    ImportManager importManager2 = getImportManager("user", "dataset");

    assertThat(importManager1, is(sameInstance(importManager2)));
  }

  public ImportManager getImportManager(String user, String dataset) throws DataStoreCreationException {
    return dataSetRepository.createDataSet(user, dataset).getImportManager();
  }

  @Test
  public void getOrCreateReturnsADifferentDataSetForDifferentDataSetIds() throws DataStoreCreationException {
    ImportManager importManager1 = getImportManager("user", "dataset");
    ImportManager importManager2 = getImportManager("user", "other");

    assertThat(importManager1, is(not(sameInstance(importManager2))));
  }

  @Test
  public void getOrCreateReturnsADifferentDataSetForDifferentUserIds() throws DataStoreCreationException {
    ImportManager importManager1 = getImportManager("user", "dataset");
    ImportManager importManager2 = getImportManager("other", "dataset");

    assertThat(importManager1, is(not(sameInstance(importManager2))));
  }

  @Test
  public void createImportManagerOnlyAddsANewDataSetToResourceSync() throws Exception {
    dataSetRepository.createDataSet("user", "dataset");
    dataSetRepository.createDataSet("user", "dataset");

    verify(resourceSync, times(1)).resourceList("user", "dataset");
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserIsNotKnown() {
    boolean dataSetExists = dataSetRepository.dataSetExists("ownerId", "dataSetId");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserDoesNotOwnADataSetWithTheDataSetId() throws DataStoreCreationException {
    getImportManager("ownerId", "otherDataSetId");

    boolean dataSetExists = dataSetRepository.dataSetExists("ownerId", "dataSetId");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void dataSetExistsReturnsTrueIfTheUserOwnsADataSetWithTheDataSetId() throws DataStoreCreationException {
    getImportManager("ownerId", "dataSetId");

    boolean dataSetExists = dataSetRepository.dataSetExists("ownerId", "dataSetId");

    assertThat(dataSetExists, is(true));
  }

  @Test
  public void deleteDataSetRemovesTheDataSetFromDisk() throws Exception {
    getImportManager("user", "dataSet");
    File dataSetPath = new File(new File(tempFile, "user"), "dataSet");
    assertThat(dataSetPath.exists(), is(true));

    dataSetRepository.removeDataSet("user", "dataSet");

    assertThat(dataSetPath.exists(), is(false));
  }

  @Test
  public void deleteDataSetRemovesTheDataSetFromTheIndex() throws Exception {
    getImportManager("user", "dataSet");

    dataSetRepository.removeDataSet("user", "dataSet");

    assertThat(dataSetRepository.dataSetExists("user", "dataSet"), is(false));
  }

  @Test
  public void removeDataSetRemovesItFromResourceSync() throws Exception {
    dataSetRepository.createDataSet("user", "dataSet");

    dataSetRepository.removeDataSet("user", "dataSet");

    verify(resourceSync).removeDataSet("user", "dataSet");
  }

}
