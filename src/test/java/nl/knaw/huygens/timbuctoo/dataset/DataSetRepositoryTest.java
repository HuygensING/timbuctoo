package nl.knaw.huygens.timbuctoo.dataset;

import com.google.common.io.Files;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.dataset.dto.Metadata;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataSetCreationException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.NotEnoughPermissionsException;
import nl.knaw.huygens.timbuctoo.datastorage.implementations.filesystem.FileSystemDataStorage;
import nl.knaw.huygens.timbuctoo.dropwizard.BdbNonPersistentEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.rdfio.RdfIoFactory;
import nl.knaw.huygens.timbuctoo.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.util.TimbuctooRdfIdHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.TreeMap;
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
  private PermissionFetcher permissionFetcher;

  @BeforeEach
  public void init() throws Exception {
    tempFile = Files.createTempDir();
    dataSetRepository = createDataSetRepo();
  }

  private DataSetRepository createDataSetRepo() throws IOException {
    permissionFetcher = mock(PermissionFetcher.class);
    return new DataSetRepository(
      Executors.newSingleThreadExecutor(),
      permissionFetcher,
      new BdbNonPersistentEnvironmentCreator(),
      Metadata.create(Optional.empty(), new TreeMap<>()),
      new TimbuctooRdfIdHelper("http://example.org/timbuctoo/"),
      combinedId -> {
      },
      false, new FileSystemDataStorage(tempFile.getAbsolutePath(), mock(RdfIoFactory.class, RETURNS_DEEP_STUBS))
    );
  }

  @AfterEach
  public void cleanUp() {
    tempFile.delete();
  }

  @Test
  public void createDataSetThrowsAnExceptionWhenTheDataSetAlreadyExist() throws Exception {
    Assertions.assertThrows(DataSetCreationException.class, () -> {
      dataSetRepository.createDataSet(User.create(null, "user"), "dataset");
      dataSetRepository.createDataSet(User.create(null, "user"), "dataset");
    });
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
    given(permissionFetcher.hasPermission(user, dataSet.getMetadata(), Permission.REMOVE_DATASET)).willReturn(true);

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset", user);

    assertThat(dataSetPath.exists(), is(false));
  }

  @Test
  public void removeDataSetRemovesTheDataSetFromTheIndex() throws Exception {
    User user = User.create(null, "user");
    final DataSet dataSet = dataSetRepository.createDataSet(user,"dataset");
    given(permissionFetcher.hasPermission(user, dataSet.getMetadata(), Permission.REMOVE_DATASET)).willReturn(true);

    dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset", user);

    assertThat(dataSetRepository.dataSetExists(dataSet.getMetadata().getOwnerId(), "dataset"), is(false));
  }

  @Test
  public void removeDataSetThrowsAnExceptionWhenTheUserHasNoAdminPermissions() throws Exception {
    Assertions.assertThrows(NotEnoughPermissionsException.class, () -> {
      User user = User.create(null, "user");
      final DataSet dataSet = dataSetRepository.createDataSet(user, "dataset");
      User userWithOutPermissions = User.create(null, "userWithOutPermissions");

      dataSetRepository.removeDataSet(dataSet.getMetadata().getOwnerId(), "dataset", userWithOutPermissions);
    });
  }

  @Test
  public void removeDataSetRemovesTheDataSetsAuthorizations() throws Exception {
    User user = User.create(null, "user");
    final DataSet dataSet = dataSetRepository.createDataSet(user, "dataset" );
    DataSetMetaData metadata = dataSet.getMetadata();
    String owner = metadata.getOwnerId();
    given(permissionFetcher.hasPermission(user, dataSet.getMetadata(), Permission.REMOVE_DATASET)).willReturn(true);

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
  public void publishDataSetWillReturnDataSetMetaDataWithPublishedSetToTrue() throws Exception {
    User user = User.create(null, "user");
    DataSet dataSet = dataSetRepository.createDataSet(user, "dataset");
    DataSetMetaData metadata = dataSet.getMetadata();
    given(permissionFetcher.hasPermission(user, dataSet.getMetadata(), Permission.PUBLISH_DATASET)).willReturn(true);
    given(permissionFetcher.hasPermission(user, dataSet.getMetadata(), Permission.READ)).willReturn(true);

    assertThat(metadata.isPublished(), is(false));

    dataSetRepository.publishDataSet(user, "uuser", "dataset");

    assertThat(metadata.isPublished(), is(true));
  }
}
