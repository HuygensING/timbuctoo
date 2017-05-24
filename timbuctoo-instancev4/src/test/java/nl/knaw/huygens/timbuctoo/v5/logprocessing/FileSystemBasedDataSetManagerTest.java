package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FileSystemBasedDataSetManagerTest {

  private FileSystemBasedDataSetManager dataSetManager;
  private File tempDir;

  @Before
  public void make() {
    tempDir = Files.createTempDir();
    dataSetManager = new FileSystemBasedDataSetManager(tempDir);
  }

  @After
  public void cleanUp() {
    tempDir.delete();
  }


  @Test
  public void getsertCreatesANewDatasetOnTheFileSystem() {
    assertThat(tempDir.listFiles().length, is(0));
    DataSet dataSet = dataSetManager.getsert("user", "dataset");
    assertThat(tempDir.listFiles().length, is(1));
  }

}
