package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import java.util.Optional;

public class DataSetProcessor implements Runnable {
  private DataSet dataSet;

  public DataSetProcessor(DataSet dataSet) {
    this.dataSet = dataSet;
  }

  @Override
  public void run() {
    while (!dataSet.isUpToDate()) {
      Optional<RdfLogEntry> logPart = dataSet.nextLogToProcess();
      logPart.ifPresent(RdfLogEntry::execute);
    }
  }
}
