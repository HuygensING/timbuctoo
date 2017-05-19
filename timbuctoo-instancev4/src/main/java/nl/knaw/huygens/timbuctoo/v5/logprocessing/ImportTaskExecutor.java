package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * This class makes sure the imports are completed.
 */
public class ImportTaskExecutor {
  private final Set<DataSet> dataSets;
  private ExecutorService executorService;

  public ImportTaskExecutor(Set<DataSet> dataSets, ExecutorService executorService) {
    this.dataSets = dataSets;
    this.executorService = executorService;
  }

  public void registerDataSet(DataSet dataSet) {
    this.dataSets.add(dataSet);
  }

  public void registerLogForDataset(LogPart logPart, String dataSetName) {
    dataSets.stream().filter(dataSet -> dataSetName.equals(dataSet.getName())).findAny().get().addLogPart(logPart);
  }

  public Map<String, DataSet.DataSetStatus> getStatus() {
    return dataSets.stream().collect(Collectors.toMap(DataSet::getName, DataSet::getStatus));
  }

}
