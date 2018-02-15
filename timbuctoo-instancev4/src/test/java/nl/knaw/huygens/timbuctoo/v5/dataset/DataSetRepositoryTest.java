package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.NotEnoughPermissionsException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.BdbNonPersistentEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DataSetRepositoryTest {

  protected File tempFile;
  private DataSetRepository dataSetRepository;
  //private ResourceSync resourceSync;
  private File authDir;
  private PermissionFetcher permissionFetcher;

  @Before
  public void init() throws Exception {
    tempFile = Files.createTempDir();
    //resourceSync = mock(ResourceSync.class);
    //when(resourceSync.getDataSetDescriptionFile(anyString(), anyString())).thenReturn(new File(tempFile, "test.xml"));
    dataSetRepository = createDataSetRepo();
  }

  private DataSetRepository createDataSetRepo() throws IOException {
    authDir = tempFile;
    permissionFetcher = mock(PermissionFetcher.class);
    return new DataSetRepository(
      Executors.newSingleThreadExecutor(),
      permissionFetcher,
      ImmutableDataSetConfiguration.builder()
        .dataSetMetadataLocation(tempFile.getAbsolutePath())
        .rdfIo(mock(RdfIoFactory.class, RETURNS_DEEP_STUBS))
        .fileStorage(mock(FileStorageFactory.class, RETURNS_DEEP_STUBS))
        //.resourceSync(resourceSync)
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

  // @Test
  // public void createImportManagerOnlyAddsANewDataSetToResourceSync() throws Exception {
  //   dataSetRepository.createDataSet(User.create(null, "user"), "dataset");
  //   dataSetRepository.createDataSet(User.create(null, "user"), "dataset");
  //
  //   verify(resourceSync, times(1)).resourceList("uuser", "dataset");
  // }

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
    given(permissionFetcher.getPermissions(user, dataSet.getMetadata())).willReturn(Sets.newHashSet(Permission.ADMIN));

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset", user);

    assertThat(dataSetPath.exists(), is(false));
  }

  @Test
  public void removeDataSetRemovesTheDataSetFromTheIndex() throws Exception {
    User user = User.create(null, "user");
    final DataSet dataSet = dataSetRepository.createDataSet(user,"dataset");
    given(permissionFetcher.getPermissions(user, dataSet.getMetadata())).willReturn(Sets.newHashSet(Permission.ADMIN));

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset", user);

    assertThat(dataSetRepository.dataSetExists(dataSet.getMetadata().getOwnerId(), "dataset"), is(false));
  }

  // @Test
  // public void removeDataSetRemovesItFromResourceSync() throws Exception {
  //   User user = User.create(null, "user");
  //   final DataSet dataSet = dataSetRepository.createDataSet(user,"dataset");
  //given(permissionFetcher.getPermissions(user, dataSet.getMetadata())).willReturn(Sets.newHashSet(Permission.ADMIN));
  //
  //   dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset", user);
  //
  //   verify(resourceSync).removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset");
  // }

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
    DataSetMetaData metadata = dataSet.getMetadata();
    String owner = metadata.getOwnerId();
    given(permissionFetcher.getPermissions(user, metadata)).willReturn(Sets.newHashSet(Permission.ADMIN));

    dataSetRepository.removeDataSet(owner, "dataset", user);

    verify(permissionFetcher).removeAuthorizations(metadata.getCombinedId());
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
  public void publishDataSetWillReturnDataSetMetaDataWithPublishedFlagSet() throws Exception {
    User user = User.create(null, "user");
    DataSet dataSet = dataSetRepository.createDataSet(user, "dataset");
    DataSetMetaData metadata = dataSet.getMetadata();
    given(permissionFetcher.getPermissions(user, dataSet.getMetadata())).willReturn(
      Sets.newHashSet(Permission.ADMIN, Permission.READ)
    );

    assertThat(metadata.isPublished(), is(false));

    dataSetRepository.publishDataSet(user, "uuser", "dataset");

    assertThat(metadata.isPublished(), is(true));
  }

}
