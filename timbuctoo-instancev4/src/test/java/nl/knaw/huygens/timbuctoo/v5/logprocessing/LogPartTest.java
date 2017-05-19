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

public class LogPartTest {

  @Test
  public void isUpToDateWillReturnTrueWhenAllTheProcessStepsAreDone() {
    LogPart instance = new LogPart(Lists.newArrayList());

    boolean upToDate = instance.isUpToDate();

    assertThat(upToDate, is(true));
  }

  @Test
  public void isUpToDateWillReturnFalseWhenSomeProcessStepsAreNotDone() {
    LogPart.ProcessStep processStep1 =
      processStepDone(LogPart.ImportStepStatus.TODO);
    LogPart.ProcessStep processStep = processStep1;
    LogPart instance = new LogPart(Lists.newArrayList(processStep));

    boolean upToDate = instance.isUpToDate();

    assertThat(upToDate, is(false));
  }

  @Test
  public void executeWillExecuteAllTheLogStepsThatAreNotDoneInSequence() {
    LogPart.ProcessStep processStepToDo1 = processStepDone(LogPart.ImportStepStatus.TODO);
    LogPart.ProcessStep processStepToDo2 = processStepDone(LogPart.ImportStepStatus.TODO);
    LogPart.ProcessStep processStepDone = processStepDone(LogPart.ImportStepStatus.DONE);
    LogPart instance = new LogPart(Lists.newArrayList(processStepDone, processStepToDo1, processStepToDo2));

    instance.execute();

    verify(processStepDone, never()).execute();
    InOrder inOrder = inOrder(processStepToDo1, processStepToDo2);
    inOrder.verify(processStepToDo1).execute();
    inOrder.verify(processStepToDo2).execute();
  }

  @Test
  public void executeWillFirstExecuteTheTasksWithTheStatusExecuting() {
    LogPart.ProcessStep processStepToDo1 = processStepDone(LogPart.ImportStepStatus.TODO);
    LogPart.ProcessStep processStepExecuting = processStepDone(LogPart.ImportStepStatus.EXECUTING);
    LogPart.ProcessStep processStepToDo2 = processStepDone(LogPart.ImportStepStatus.TODO);
    LogPart instance = new LogPart(Lists.newArrayList(processStepToDo1, processStepExecuting, processStepToDo2));

    instance.execute();

    InOrder inOrder = inOrder(processStepToDo1, processStepExecuting);
    inOrder.verify(processStepExecuting).execute();
    inOrder.verify(processStepToDo1).execute();
  }

  private LogPart.ProcessStep processStepDone(LogPart.ImportStepStatus done) {
    LogPart.ProcessStep processStep = mock(LogPart.ProcessStep.class);
    given(processStep.getStatus()).willReturn(done);
    return processStep;
  }
}
