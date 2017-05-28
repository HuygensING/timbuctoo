package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.google.common.io.Files;
import nl.knaw.huygens.timbuctoo.v5.datastores.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
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
  public void init() throws IOException {
    tempFile = Files.createTempDir();
    dataSetFactory = new DataSetFactory(
      Executors.newSingleThreadExecutor(),
      ImmutableDataSetConfiguration.builder()
        .dataSetMetadataLocation(tempFile.getAbsolutePath())
        .rdfIo(mock(RdfIoFactory.class, RETURNS_DEEP_STUBS))
        .fileStorage(mock(FileStorageFactory.class, RETURNS_DEEP_STUBS))
        .dataStore(mock(DataStoreFactory.class, RETURNS_DEEP_STUBS))
        .build()
    );
  }

  @After
  public void cleanUp() {
    tempFile.delete();
  }

  @Test
  public void getOrCreateReturnsTheSamesDataSetForEachCall() throws DataStoreCreationException {
    DataSet dataSet1 = dataSetFactory.getOrCreate("user", "dataset");
    DataSet dataSet2 = dataSetFactory.getOrCreate("user", "dataset");

    assertThat(dataSet1, is(sameInstance(dataSet2)));
  }

  @Test
  public void getOrCreateReturnsADifferentDataSetForDifferentDataSetIds() throws DataStoreCreationException {
    DataSet dataSet1 = dataSetFactory.getOrCreate("user", "dataset");
    DataSet dataSet2 = dataSetFactory.getOrCreate("user", "other");

    assertThat(dataSet1, is(not(sameInstance(dataSet2))));
  }

  @Test
  public void getOrCreateReturnsADifferentDataSetForDifferentUserIds() throws DataStoreCreationException {
    DataSet dataSet1 = dataSetFactory.getOrCreate("user", "dataset");
    DataSet dataSet2 = dataSetFactory.getOrCreate("other", "dataset");

    assertThat(dataSet1, is(not(sameInstance(dataSet2))));
  }

}
