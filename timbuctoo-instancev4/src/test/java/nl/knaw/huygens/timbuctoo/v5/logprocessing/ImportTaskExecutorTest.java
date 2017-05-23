package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.DataSet.DataSetStatus;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ImportTaskExecutorTest {

  private ExecutorService executorServiceMock;

  @Before
  public void setUp() throws Exception {
    executorServiceMock = mock(ExecutorService.class);
  }

  @Test
  public void registerDataSetAddsANewDataSetToTheImportManager() {
    ImportTaskExecutor instance = new ImportTaskExecutor(executorServiceMock);
    DataSet dataSet = DataSetStubs.dataSetWithNameAndStatus("dataSet", mock(DataSetStatus.class));

    instance.registerDataSet(dataSet);

    assertThat(instance.getStatus(), hasKey("dataSet"));
  }

  @Test
  public void registerImportLogForDataSetAddsAnImportLogToTheDataSet() {
    DataSet dataSet = DataSetStubs.dataSetWithName("dataSet");
    ImportTaskExecutor instance = new ImportTaskExecutor(executorServiceMock);
    instance.registerDataSet(dataSet);
    RdfLogEntry rdfLogEntry = mock(RdfLogEntry.class);

    instance.registerLogForDataset(rdfLogEntry, "dataSet");

    verify(dataSet).addLogPart(rdfLogEntry);
  }

  @Test
  public void getStatusReturnsAnAggregatedStatusOfTheDataSets() {
    DataSetStatus status1 = mock(DataSetStatus.class);
    DataSet dataSet1 = DataSetStubs.dataSetWithNameAndStatus("dataSet1", status1);
    DataSetStatus status2 = mock(DataSetStatus.class);
    DataSet dataSet2 = DataSetStubs.dataSetWithNameAndStatus("dataSet2", status2);
    ImportTaskExecutor instance = new ImportTaskExecutor(executorServiceMock);
    instance.registerDataSet(dataSet1);
    instance.registerDataSet(dataSet2);

    Map<String, DataSetStatus> status = instance.getStatus();

    assertThat(status, allOf(hasEntry("dataSet1", status1), hasEntry("dataSet2", status2)));
  }

}
