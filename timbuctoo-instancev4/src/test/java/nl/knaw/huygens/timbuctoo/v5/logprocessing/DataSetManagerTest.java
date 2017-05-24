package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import com.google.common.io.Files;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

@RunWith(Parameterized.class)
public class DataSetManagerTest {

  private DataSetManager dataSetManager;

  public DataSetManagerTest(DataSetManager dataSetManager, String className) {

    this.dataSetManager = dataSetManager;
  }

  @Test
  public void getsertReturnsTheSamesDataSetForEachCall() {
    DataSet dataSet1 = dataSetManager.getsert("user", "dataset");
    DataSet dataSet2 = dataSetManager.getsert("user", "dataset");

    assertThat(dataSet1, is(sameInstance(dataSet2)));
  }

  @Test
  public void getsertReturnsADifferentDataSetForDifferentDataSetIds() {
    DataSet dataSet1 = dataSetManager.getsert("user", "dataset");
    DataSet dataSet2 = dataSetManager.getsert("user", "other");

    assertThat(dataSet1, is(not(sameInstance(dataSet2))));
  }

  @Test
  public void getsertReturnsADifferentDataSetForDifferentUserIds() {
    DataSet dataSet1 = dataSetManager.getsert("user", "dataset");
    DataSet dataSet2 = dataSetManager.getsert("other", "dataset");

    assertThat(dataSet1, is(not(sameInstance(dataSet2))));
  }

  @Parameterized.Parameters(name = "{1}")
  public static Collection<Object[]> instancesToTest() {
    File tempDir = Files.createTempDir();
    return Lists.<Object[]>newArrayList(
      new Object[]{new FileSystemBasedDataSetManager(tempDir), FileSystemBasedDataSetManager.class.getName()}
    );
  }
}
