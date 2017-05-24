package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * This class makes sure the imports are completed.
 */
public class ImportTaskExecutor {
  private final Set<DataSet> dataSets;
  private ExecutorService executorService;

  public ImportTaskExecutor(ExecutorService executorService) {
    this.dataSets = Sets.newHashSet();
    this.executorService = executorService;
  }

  public void registerDataSet(DataSet dataSet) {
    this.dataSets.add(dataSet);
    if (!dataSet.isUpToDate()) {
      this.executorService.submit(new DataSetProcessor(dataSet));
    }
  }

  public void registerLogForDataset(RdfLogEntry rdfLogEntry, String dataSetName) {
    Optional<DataSet> dataSetOptional = dataSets.stream()
                                                .filter(dataSet -> dataSetName.equals(dataSet.getName())).findAny();
    dataSetOptional.ifPresent(dataSet -> {
      dataSet.addLogPart(rdfLogEntry);
      if(!dataSet.isUpToDate()){
        executorService.submit(new DataSetProcessor(dataSet));
      }
    });

  }

  public Map<String, DataSet.DataSetStatus> getStatus() {
    return dataSets.stream().collect(Collectors.toMap(DataSet::getName, DataSet::getStatus));
  }

}
