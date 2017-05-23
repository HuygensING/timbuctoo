package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import org.assertj.core.util.Lists;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RdfLogEntryStubs {

  public static RdfLogEntry logPartUpToDate() {
    RdfLogEntry rdfLogEntry = mock(RdfLogEntry.class);
    given(rdfLogEntry.isUpToDate()).willReturn(true);

    return rdfLogEntry;
  }

  public static RdfLogEntry logPartNotUpToDate() {
    RdfLogEntry.ProcessStep processStep = processStep();

    RdfLogEntry rdfLogEntry = new RdfLogEntry(Lists.newArrayList(processStep), null, null, 0);


    return rdfLogEntry;
  }

  private static RdfLogEntry.ProcessStep processStep() {
    return new RdfLogEntry.ProcessStep() {

      private RdfLogEntry.ProcessStepStatus status = RdfLogEntry.ProcessStepStatus.TODO;

      @Override
      public int getLastLineDone() {
        return 0;
      }

      @Override
      public RdfLogEntry.ProcessStepStatus getStatus() {
        return status;
      }

      @Override
      public void execute() {
        status = RdfLogEntry.ProcessStepStatus.DONE;
      }
    };
  }
}
