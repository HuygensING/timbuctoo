package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.io.Files;
import nl.knaw.huygens.timbuctoo.security.BasicPermissionFetcher;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthorizer;
import nl.knaw.huygens.timbuctoo.security.dataaccess.localfile.LocalFileVreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.BdbNonPersistentEnvironmentCreator;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataSetRepositoryTest {

  protected File tempFile;
  private DataSetRepository dataSetRepository;
  private ResourceSync resourceSync;

  @Before
  public void init() throws IOException, InterruptedException {
    tempFile = Files.createTempDir();
    resourceSync = mock(ResourceSync.class);
    when(resourceSync.getDataSetDescriptionFile(anyString(), anyString())).thenReturn(new File(tempFile, "test.xml"));
    dataSetRepository = createDataSetRepo();
  }

  private DataSetRepository createDataSetRepo() throws IOException {
    return new DataSetRepository(
      Executors.newSingleThreadExecutor(),
      new BasicPermissionFetcher(new JsonBasedAuthorizer(new LocalFileVreAuthorizationAccess(tempFile.toPath())), null),
      ImmutableDataSetConfiguration.builder()
        .dataSetMetadataLocation(tempFile.getAbsolutePath())
        .rdfIo(mock(RdfIoFactory.class, RETURNS_DEEP_STUBS))
        .fileStorage(mock(FileStorageFactory.class, RETURNS_DEEP_STUBS))
        .resourceSync(resourceSync)
        .build(),
      new BdbNonPersistentEnvironmentCreator(),
      new TimbuctooRdfIdHelper("http://example.org/timbuctoo/"),
      combinedId -> {
      }
    );
  }

  @After
  public void cleanUp() {
    tempFile.delete();
  }

  @Test
  public void createDataSetReturnsTheSamesDataSetForEachCall() throws Exception {
    DataSet dataSet1 = dataSetRepository.createDataSet(User.create(null, "user"), "dataset", false);
    DataSet dataSet2 = dataSetRepository.createDataSet(User.create(null, "user"), "dataset", false);

    assertThat(dataSet1, is(sameInstance(dataSet2)));
  }


  public ImportManager getImportManager(String user, String dataset) throws Exception {
    return dataSetRepository.createDataSet(User.create(null, user), dataset, false).getImportManager();
  }

  @Test
  public void createDataSetReturnsADifferentDataSetForDifferentDataSetIds() throws Exception {
    ImportManager importManager1 = getImportManager("user", "dataset");
    ImportManager importManager2 = getImportManager("user", "other");

    assertThat(importManager1, is(not(sameInstance(importManager2))));
  }

  @Test
  public void createDataSetReturnsADifferentDataSetForDifferentUserIds() throws Exception {
    ImportManager importManager1 = getImportManager("user", "dataset");
    ImportManager importManager2 = getImportManager("other", "dataset");

    assertThat(importManager1, is(not(sameInstance(importManager2))));
  }

  @Test
  public void createImportManagerOnlyAddsANewDataSetToResourceSync() throws Exception {
    dataSetRepository.createDataSet(User.create(null, "user"), "dataset", false);
    dataSetRepository.createDataSet(User.create(null, "user"), "dataset", false);

    verify(resourceSync, times(1)).resourceList("uuser", "dataset");
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserIsNotKnown() {
    boolean dataSetExists = dataSetRepository.dataSetExists("ownerid", "dataset_id");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserDoesNotOwnADataSetWithTheDataSetId() throws Exception {
    dataSetRepository.createDataSet(User.create(null, "ownerid"), "otherdatasetid", false);

    boolean dataSetExists = dataSetRepository.dataSetExists("ownerid", "dataset_id");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void removeDataSetRemovesTheDataSetFromDisk() throws Exception {
    final DataSet dataSet = dataSetRepository.createDataSet(User.create(null, "user"), "dataset", false);
    File dataSetPath = new File(new File(tempFile, dataSet.getMetadata().getOwnerId()), "dataset");
    assertThat(dataSetPath.exists(), is(true));

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset");

    assertThat(dataSetPath.exists(), is(false));
  }

  @Test
  public void removeDataSetRemovesTheDataSetFromTheIndex() throws Exception {
    final DataSet dataSet = dataSetRepository.createDataSet(User.create(null, "user"), "dataset", false);

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset");

    assertThat(dataSetRepository.dataSetExists(dataSet.getMetadata().getOwnerId(), "dataset"), is(false));
  }

  @Test
  public void removeDataSetRemovesItFromResourceSync() throws Exception {
    final DataSet dataSet = dataSetRepository.createDataSet(User.create(null, "user"), "dataset", false);

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset");

    verify(resourceSync).removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset");
  }

  @Test
  public void dataSetsWillBeTheSameAfterRestart() throws Exception {
    final DataSet dataSet = dataSetRepository.createDataSet(User.create(null, "user"), "dataset", false);

    assertThat(dataSetRepository.dataSetExists(dataSet.getMetadata().getOwnerId(), "dataset"), is(true));

    // create a new instance to simulate a restart
    dataSetRepository = createDataSetRepo();
    dataSetRepository.start();

    assertThat(dataSetRepository.dataSetExists(dataSet.getMetadata().getOwnerId(), "dataset"), is(true));
  }

}
