package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
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
    dataSet.nextTask().ifPresent(task -> executorService.submit(task));
  }

  public void registerLogForDataset(LogPart logPart, String dataSetName) {
    dataSets.stream().filter(dataSet -> dataSetName.equals(dataSet.getName())).findAny().get().addLogImport(logPart);
  }

  public Map<String, DataSetStatus> getStatus() {
    return dataSets.stream().collect(Collectors.toMap(DataSet::getName, DataSet::getStatus));
  }

  private enum ImportStepStatus {
    TODO,
    EXECUTING,
    DONE,
    ERROR
  }

  public interface DataSet {
    String getName();

    DataSetStatus getStatus();

    void addLogImport(LogPart logPart);

    SortedSet<LogPart> getTodoList();

    SortedSet<LogPart> getDoneList();

    SortedSet<LogPart> getErrorList();

    Optional<LogPart> nextTask();
  }

  public interface LogPart extends Runnable {
    List<TaskStep> getStatus();
  }

  private interface TaskStep {
    int getLastLineDone();

    ImportStepStatus getStatus();
  }

  public static class DataSetStatus {
  }

}
