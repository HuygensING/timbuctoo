package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.ImportTaskExecutor.DataSet;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.ImportTaskExecutor.DataSetStatus;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.ImportTaskExecutor.LogPart;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.BDDMockito.given;
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
    Set<DataSet> dataSets = Sets.newHashSet();
    ImportTaskExecutor instance = new ImportTaskExecutor(dataSets, executorServiceMock);
    DataSet dataSet = DataSetStubs.dataSetWithNameAndStatus("dataSet", mock(DataSetStatus.class));

    instance.registerDataSet(dataSet);

    assertThat(instance.getStatus(), hasKey("dataSet"));
  }

  @Test
  public void registerDataSetAddsTheTaskToTheExecutorService() {
    Set<DataSet> dataSets = Sets.newHashSet();
    ImportTaskExecutor instance = new ImportTaskExecutor(dataSets, executorServiceMock);
    LogPart logPart = mock(LogPart.class);
    DataSet dataSet = DataSetStubs.dataSetWithFirstTask(logPart);

    instance.registerDataSet(dataSet);

    verify(executorServiceMock).submit(logPart);
  }

  @Test
  public void registerImportLogForDataSetAddsAnImportLogToTheDataSet() {
    DataSet dataSet = DataSetStubs.dataSetWithName("dataSet");
    Set<DataSet> dataSets = Sets.newHashSet();
    dataSets.add(dataSet);
    ImportTaskExecutor instance = new ImportTaskExecutor(dataSets, executorServiceMock);
    LogPart logPart = mock(LogPart.class);

    instance.registerLogForDataset(logPart, "dataSet");

    verify(dataSet).addLogImport(logPart);
  }

  @Test
  public void getStatusReturnsAnAggregatedStatusOfTheDataSets() {
    DataSetStatus status1 = mock(DataSetStatus.class);
    DataSet dataSet1 = DataSetStubs.dataSetWithNameAndStatus("dataSet1", status1);
    DataSetStatus status2 = mock(DataSetStatus.class);
    DataSet dataSet2 = DataSetStubs.dataSetWithNameAndStatus("dataSet2", status2);
    Set<DataSet> dataSets = Sets.newHashSet();
    dataSets.add(dataSet1);
    dataSets.add(dataSet2);
    ImportTaskExecutor instance = new ImportTaskExecutor(dataSets, executorServiceMock);

    Map<String, DataSetStatus> status = instance.getStatus();

    assertThat(status, allOf(hasEntry("dataSet1", status1), hasEntry("dataSet2", status2)));
  }

  private static class DataSetStubs {
    public static DataSet dataSetWithName(String name) {
      DataSet dataSet = mock(DataSet.class);
      given(dataSet.getName()).willReturn(name);
      return dataSet;
    }

    public static DataSet dataSetWithNameAndStatus(String name, DataSetStatus dataSetStatus) {
      DataSet dataSet = mock(DataSet.class);
      given(dataSet.getName()).willReturn(name);
      given(dataSet.getStatus()).willReturn(dataSetStatus);
      return dataSet;
    }

    private static DataSet dataSetWithFirstTask(LogPart logPart) {
      DataSet dataSet = mock(DataSet.class);
      given(dataSet.nextTask()).willReturn(Optional.of(logPart));
      return dataSet;
    }
  }
}
