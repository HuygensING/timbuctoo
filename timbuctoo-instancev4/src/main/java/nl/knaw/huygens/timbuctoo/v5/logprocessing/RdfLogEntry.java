package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;

import java.util.List;

public class RdfLogEntry implements DataSetLogEntry {

  private final List<ProcessStep> processSteps;
  private final LocalData logFile;
  private final DataStores dataStores;
  private long version;

  public RdfLogEntry(List<ProcessStep> processSteps, LocalData logFile,
                     DataStores dataStores, long version) {
    this.processSteps = processSteps;
    this.logFile = logFile;
    this.dataStores = dataStores;
    this.version = version;
  }

  public boolean isUpToDate() {
    return processSteps.stream().allMatch(processStep -> processStep.getStatus() == ProcessStepStatus.DONE);
  }

  public List<ProcessStep> getStatus() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  public void execute() {
    processSteps.stream()
                .filter(processStep -> processStep.getStatus() != ProcessStepStatus.DONE)
                .sorted((o1, o2) -> o2.getStatus().compareTo(o1.getStatus()))
                .forEach(processStep -> processStep.execute());
  }

  @Override
  public LocalData getData() {
    return this.logFile;
  }

  @Override
  public long getVersion() {
    return this.version;
  }

  enum ProcessStepStatus {
    TODO,
    EXECUTING,
    DONE,
    ERROR
  }

  public interface ProcessStep {
    int getLastLineDone();

    ProcessStepStatus getStatus();

    void execute();
  }
}
