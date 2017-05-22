package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import java.util.List;

public class LogPart {


  private List<ProcessStep> processSteps;

  public LogPart(List<ProcessStep> processSteps) {
    this.processSteps = processSteps;
  }

  public boolean isUpToDate() {
    return processSteps.stream().allMatch(processStep -> processStep.getStatus() == ProcessStepStatus.DONE);
  }

  public List<ProcessStep> getStatus() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  void execute() {
    processSteps.stream()
                .filter(processStep -> processStep.getStatus() != ProcessStepStatus.DONE)
                .sorted((o1, o2) -> o2.getStatus().compareTo(o1.getStatus()))
                .forEach(processStep -> processStep.execute());
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
