package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import org.assertj.core.util.Lists;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class LogPartStubs {

  public static LogPart logPartUpToDate() {
    LogPart logPart = mock(LogPart.class);
    given(logPart.isUpToDate()).willReturn(true);

    return logPart;
  }

  public static LogPart logPartNotUpToDate() {
    LogPart.ProcessStep processStep = processStep();

    LogPart logPart = new LogPart(Lists.newArrayList(processStep));


    return logPart;
  }

  private static LogPart.ProcessStep processStep() {
    return new LogPart.ProcessStep() {

      private LogPart.ProcessStepStatus status = LogPart.ProcessStepStatus.TODO;

      @Override
      public int getLastLineDone() {
        return 0;
      }

      @Override
      public LogPart.ProcessStepStatus getStatus() {
        return status;
      }

      @Override
      public void execute() {
        status = LogPart.ProcessStepStatus.DONE;
      }
    };
  }
}
