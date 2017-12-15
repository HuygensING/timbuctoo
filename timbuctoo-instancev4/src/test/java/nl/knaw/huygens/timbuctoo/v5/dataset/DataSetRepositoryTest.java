package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.io.Files;
import nl.knaw.huygens.timbuctoo.security.BasicPermissionFetcher;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthorizer;
import nl.knaw.huygens.timbuctoo.security.dataaccess.localfile.LocalFileVreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataSetPublishException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.NotEnoughPermissionsException;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.BdbNonPersistentEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.naming.NoPermissionException;
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
  private File authDir;

  @Before
  public void init() throws Exception {
    tempFile = Files.createTempDir();
    resourceSync = mock(ResourceSync.class);
    when(resourceSync.getDataSetDescriptionFile(anyString(), anyString())).thenReturn(new File(tempFile, "test.xml"));
    dataSetRepository = createDataSetRepo();
  }

  private DataSetRepository createDataSetRepo() throws IOException {
    authDir = tempFile;
    return new DataSetRepository(
      Executors.newSingleThreadExecutor(),
      new BasicPermissionFetcher(new JsonBasedAuthorizer(new LocalFileVreAuthorizationAccess(authDir.toPath()))),
      ImmutableDataSetConfiguration.builder()
        .dataSetMetadataLocation(tempFile.getAbsolutePath())
        .rdfIo(mock(RdfIoFactory.class, RETURNS_DEEP_STUBS))
        .fileStorage(mock(FileStorageFactory.class, RETURNS_DEEP_STUBS))
        .resourceSync(resourceSync)
        .build(),
      new BdbNonPersistentEnvironmentCreator(),
      new TimbuctooRdfIdHelper("http://example.org/timbuctoo/"),
      combinedId -> {
      },
      false
    );
  }

  @After
  public void cleanUp() {
    tempFile.delete();
  }

  @Test
  public void createDataSetReturnsTheSamesDataSetForEachCall() throws Exception {
    DataSet dataSet1 = dataSetRepository.createDataSet(User.create(null, "user"), "dataset");
    DataSet dataSet2 = dataSetRepository.createDataSet(User.create(null, "user"), "dataset");

    assertThat(dataSet1, is(sameInstance(dataSet2)));
  }

  public ImportManager getImportManager(String user, String dataset) throws Exception {
    return dataSetRepository.createDataSet(User.create(null, user), dataset).getImportManager();
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
    dataSetRepository.createDataSet(User.create(null, "user"), "dataset");
    dataSetRepository.createDataSet(User.create(null, "user"), "dataset");

    verify(resourceSync, times(1)).resourceList("uuser", "dataset");
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserIsNotKnown() {
    boolean dataSetExists = dataSetRepository.dataSetExists("ownerid", "dataset_id");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserDoesNotOwnADataSetWithTheDataSetId() throws Exception {
    dataSetRepository.createDataSet(User.create(null, "ownerid"), "otherdatasetid");

    boolean dataSetExists = dataSetRepository.dataSetExists("ownerid", "dataset_id");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void removeDataSetRemovesTheDataSetFromDisk() throws Exception {
    User user = User.create(null, "user");
    final DataSet dataSet = dataSetRepository.createDataSet(user,"dataset");
    File dataSetPath = new File(new File(tempFile, dataSet.getMetadata().getOwnerId()), "dataset");
    assertThat(dataSetPath.exists(), is(true));

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset", user);

    assertThat(dataSetPath.exists(), is(false));
  }

  @Test
  public void removeDataSetRemovesTheDataSetFromTheIndex() throws Exception {
    User user = User.create(null, "user");
    final DataSet dataSet = dataSetRepository.createDataSet(user,"dataset");

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset", user);

    assertThat(dataSetRepository.dataSetExists(dataSet.getMetadata().getOwnerId(), "dataset"), is(false));
  }

  @Test
  public void removeDataSetRemovesItFromResourceSync() throws Exception {
    User user = User.create(null, "user");
    final DataSet dataSet = dataSetRepository.createDataSet(user,"dataset");

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset", user);

    verify(resourceSync).removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset");
  }

  @Test(expected = NotEnoughPermissionsException.class)
  public void removeDataSetThrowsAnExceptionWhenTheUserHasNoAdminPermissions() throws Exception {
    User user = User.create(null, "user");
    final DataSet dataSet = dataSetRepository.createDataSet(user,"dataset");
    User userWithOutPermissions = User.create(null, "userWithOutPermissions");

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset", userWithOutPermissions);
  }

  @Test
  public void removeDataSetRemovesTheDataSetsAuthorizations() throws Exception {
    User user = User.create(null, "user");
    final DataSet dataSet = dataSetRepository.createDataSet(user, "dataset" );

    String owner = dataSet.getMetadata().getOwnerId();
    String dataSetName = dataSet.getMetadata().getDataSetId();

    File authFile = new File(authDir, owner + "____" + dataSetName + ".json");
    assertThat(authFile.exists(), is(true));

    dataSetRepository.removeDataSet(owner, "dataset", user);

    assertThat(authFile.exists(), is(false));
  }

  @Test
  public void dataSetsWillBeTheSameAfterRestart() throws Exception {
    final DataSet dataSet = dataSetRepository.createDataSet(User.create(null, "user"),
      "dataset"
    );

    assertThat(dataSetRepository.dataSetExists(dataSet.getMetadata().getOwnerId(), "dataset"), is(true));

    // create a new instance to simulate a restart
    dataSetRepository = createDataSetRepo();
    dataSetRepository.start();

    assertThat(dataSetRepository.dataSetExists(dataSet.getMetadata().getOwnerId(), "dataset"), is(true));
  }

  @Test
  public void publishDataSetWillReturnDataSetMetaDataWithPublishedFlagSet() throws Exception, DataSetPublishException {
    User user = User.create(null, "user");

    dataSetRepository.createDataSet(user, "dataset");

    DataSet dataSet = dataSetRepository.getDataSet(user, "uuser", "dataset").get();

    DataSetMetaData metadata = dataSet.getMetadata();
    assertThat(metadata.isPublished(), is(false));


    dataSetRepository.publishDataSet(user, "uuser", "dataset");

    assertThat(metadata.isPublished(), is(true));
  }

}
