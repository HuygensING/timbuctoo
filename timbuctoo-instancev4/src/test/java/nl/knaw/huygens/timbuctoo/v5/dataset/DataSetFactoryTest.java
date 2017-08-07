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

public class DataSetFactoryTest {

  private DataSetFactory dataSetFactory;
  protected File tempFile;
  private ResourceSync resourceSync;

  @Before
  public void init() throws IOException, InterruptedException {
    tempFile = Files.createTempDir();
    resourceSync = mock(ResourceSync.class);
    dataSetFactory = new DataSetFactory(
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
  public void createImportManagerReturnsTheSamesDataSetForEachCall() throws DataStoreCreationException {
    ImportManager importManager1 = dataSetFactory.createImportManager("user", "dataset");
    ImportManager importManager2 = dataSetFactory.createImportManager("user", "dataset");

    assertThat(importManager1, is(sameInstance(importManager2)));
  }

  @Test
  public void createImportManagerReturnsADifferentDataSetForDifferentDataSetIds() throws DataStoreCreationException {
    ImportManager importManager1 = dataSetFactory.createImportManager("user", "dataset");
    ImportManager importManager2 = dataSetFactory.createImportManager("user", "other");

    assertThat(importManager1, is(not(sameInstance(importManager2))));
  }

  @Test
  public void createImportManagerReturnsADifferentDataSetForDifferentUserIds() throws DataStoreCreationException {
    ImportManager importManager1 = dataSetFactory.createImportManager("user", "dataset");
    ImportManager importManager2 = dataSetFactory.createImportManager("other", "dataset");

    assertThat(importManager1, is(not(sameInstance(importManager2))));
  }

  @Test
  public void createImportManagerOnlyAddsANewDataSetToResourceSync() throws Exception {
    dataSetFactory.createImportManager("user", "dataset");
    dataSetFactory.createImportManager("user", "dataset");

    verify(resourceSync, times(1)).addDataSet("user", "dataset");
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserIsNotKnown() {
    boolean dataSetExists = dataSetFactory.dataSetExists("ownerId", "dataSetId");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserDoesNotOwnADataSetWithTheDataSetId() throws DataStoreCreationException {
    dataSetFactory.createImportManager("otherOwner", "dataSetId");

    boolean dataSetExists = dataSetFactory.dataSetExists("ownerId", "dataSetId");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void dataSetExistsReturnsTrueIfTheUserOwnsADataSetWithTheDataSetId() throws DataStoreCreationException {
    dataSetFactory.createImportManager("ownerId", "dataSetId");

    boolean dataSetExists = dataSetFactory.dataSetExists("ownerId", "dataSetId");

    assertThat(dataSetExists, is(true));
  }

  @Test
  public void deleteDataSetRemovesTheDataSetFromDisk() throws Exception {
    dataSetFactory.createImportManager("user", "dataSet");
    File dataSetPath = new File(new File(tempFile, "user"), "dataSet");
    assertThat(dataSetPath.exists(), is(true));

    dataSetFactory.removeDataSet("user", "dataSet");

    assertThat(dataSetPath.exists(), is(false));
  }

  @Test
  public void deleteDataSetRemovesTheDataSetFromTheIndex() throws Exception {
    dataSetFactory.createImportManager("user", "dataSet");

    dataSetFactory.removeDataSet("user", "dataSet");

    assertThat(dataSetFactory.dataSetExists("user", "dataSet"), is(false));
  }

}
