package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.io.Files;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthorizer;
import nl.knaw.huygens.timbuctoo.security.dataaccess.localfile.LocalFileVreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
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

public class DataSetFactoryTest {

  private DataSetFactory dataSetFactory;
  protected File tempFile;

  @Before
  public void init() throws IOException, InterruptedException {
    tempFile = Files.createTempDir();
    dataSetFactory = new DataSetFactory(
      Executors.newSingleThreadExecutor(),
      new JsonBasedAuthorizer(new LocalFileVreAuthorizationAccess(tempFile.toPath())),
      ImmutableDataSetConfiguration.builder()
                                   .dataSetMetadataLocation(tempFile.getAbsolutePath())
                                   .rdfIo(mock(RdfIoFactory.class, RETURNS_DEEP_STUBS))
                                   .fileStorage(mock(FileStorageFactory.class, RETURNS_DEEP_STUBS))
                                   .build(),
      new NonPersistentBdbDatabaseCreator()
    );
  }

  @After
  public void cleanUp() {
    tempFile.delete();
  }

  @Test
  public void getOrCreateReturnsTheSamesDataSetForEachCall() throws DataStoreCreationException {
    DataSet dataSet1 = dataSetFactory.createDataSet("user", "dataset");
    DataSet dataSet2 = dataSetFactory.createDataSet("user", "dataset");

    assertThat(dataSet1, is(sameInstance(dataSet2)));
  }

  @Test
  public void getOrCreateReturnsADifferentDataSetForDifferentDataSetIds() throws DataStoreCreationException {
    DataSet dataSet1 = dataSetFactory.createDataSet("user", "dataset");
    DataSet dataSet2 = dataSetFactory.createDataSet("user", "other");

    assertThat(dataSet1, is(not(sameInstance(dataSet2))));
  }

  @Test
  public void getOrCreateReturnsADifferentDataSetForDifferentUserIds() throws DataStoreCreationException {
    DataSet dataSet1 = dataSetFactory.createDataSet("user", "dataset");
    DataSet dataSet2 = dataSetFactory.createDataSet("other", "dataset");

    assertThat(dataSet1, is(not(sameInstance(dataSet2))));
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserIsNotKnown() {
    boolean dataSetExists = dataSetFactory.dataSetExists("ownerId", "dataSetId");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void dataSetExistsReturnsFalseIfTheUserDoesNotOwnADataSetWithTheDataSetId() throws DataStoreCreationException {
    dataSetFactory.createDataSet("ownerId", "otherDataSetId");

    boolean dataSetExists = dataSetFactory.dataSetExists("ownerId", "dataSetId");

    assertThat(dataSetExists, is(false));
  }

  @Test
  public void dataSetExistsReturnsTrueIfTheUserOwnsADataSetWithTheDataSetId() throws DataStoreCreationException {
    dataSetFactory.createDataSet("ownerId", "dataSetId");

    boolean dataSetExists = dataSetFactory.dataSetExists("ownerId", "dataSetId");

    assertThat(dataSetExists, is(true));
  }

}
