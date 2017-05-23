package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.mockito.InOrder;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class RdfLogEntryTest {

  @Test
  public void isUpToDateWillReturnTrueWhenAllTheProcessStepsAreDone() {
    RdfLogEntry instance = new RdfLogEntry(Lists.newArrayList(), null, null, 0);

    boolean upToDate = instance.isUpToDate();

    assertThat(upToDate, is(true));
  }

  @Test
  public void isUpToDateWillReturnFalseWhenSomeProcessStepsAreNotDone() {
    RdfLogEntry.ProcessStep processStep1 =
      processStepDone(RdfLogEntry.ProcessStepStatus.TODO);
    RdfLogEntry.ProcessStep processStep = processStep1;
    RdfLogEntry instance = new RdfLogEntry(Lists.newArrayList(processStep), null, null, 0);

    boolean upToDate = instance.isUpToDate();

    assertThat(upToDate, is(false));
  }

  @Test
  public void executeWillExecuteAllTheLogStepsThatAreNotDoneInSequence() {
    RdfLogEntry.ProcessStep processStepToDo1 = processStepDone(RdfLogEntry.ProcessStepStatus.TODO);
    RdfLogEntry.ProcessStep processStepToDo2 = processStepDone(RdfLogEntry.ProcessStepStatus.TODO);
    RdfLogEntry.ProcessStep processStepDone = processStepDone(RdfLogEntry.ProcessStepStatus.DONE);
    RdfLogEntry
      instance = new RdfLogEntry(Lists.newArrayList(processStepDone, processStepToDo1, processStepToDo2),
      null, null, 0);

    instance.execute();

    verify(processStepDone, never()).execute();
    InOrder inOrder = inOrder(processStepToDo1, processStepToDo2);
    inOrder.verify(processStepToDo1).execute();
    inOrder.verify(processStepToDo2).execute();
  }

  @Test
  public void executeWillFirstExecuteTheTasksWithTheStatusExecuting() {
    RdfLogEntry.ProcessStep processStepToDo1 = processStepDone(RdfLogEntry.ProcessStepStatus.TODO);
    RdfLogEntry.ProcessStep processStepExecuting = processStepDone(RdfLogEntry.ProcessStepStatus.EXECUTING);
    RdfLogEntry.ProcessStep processStepToDo2 = processStepDone(RdfLogEntry.ProcessStepStatus.TODO);
    RdfLogEntry instance = new RdfLogEntry(Lists.newArrayList(processStepToDo1, processStepExecuting, processStepToDo2),
      null, null, 0);

    instance.execute();

    InOrder inOrder = inOrder(processStepToDo1, processStepExecuting);
    inOrder.verify(processStepExecuting).execute();
    inOrder.verify(processStepToDo1).execute();
  }

  private RdfLogEntry.ProcessStep processStepDone(RdfLogEntry.ProcessStepStatus done) {
    RdfLogEntry.ProcessStep processStep = mock(RdfLogEntry.ProcessStep.class);
    given(processStep.getStatus()).willReturn(done);
    return processStep;
  }
}
