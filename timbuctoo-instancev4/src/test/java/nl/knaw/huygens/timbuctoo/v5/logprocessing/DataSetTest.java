package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.mockito.InOrder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class DataSetTest {

  @Test
  public void isUpToDateReturnsTrueIfThereANoLogsThatAreNotUpToDate() {
    DataSet instance = new DataSet("name");

    boolean upToDate = instance.isUpToDate();

    assertThat(upToDate, is(true));
  }

  @Test
  public void isUpToDateReturnsFalseIfALogIsNotUpToDate() {
    DataSet instance = new DataSet("name");
    LogPart logPart = mock(LogPart.class);
    given(logPart.isUpToDate()).willReturn(false);
    instance.addLogPart(logPart);

    boolean upToDate = instance.isUpToDate();

    assertThat(upToDate, is(false));
  }

  @Test
  public void runExecutesTheFirstLogPartThatIsNotUpToDate() {
    LogPart logPartUpToDate = mock(LogPart.class);
    given(logPartUpToDate.isUpToDate()).willReturn(true);
    LogPart logPartNotUpToDate = mock(LogPart.class);
    given(logPartNotUpToDate.isUpToDate()).willReturn(false);

    DataSet instance = new DataSet("name", Lists.newArrayList(logPartUpToDate, logPartNotUpToDate));

    instance.run();

    verify(logPartUpToDate, never()).execute();
    verify(logPartNotUpToDate).execute();
  }

  @Test
  public void runExecutesNotUpToDateLogPartsInSequence() {
    LogPart logPart1 = mock(LogPart.class);
    given(logPart1.isUpToDate()).willReturn(false);
    LogPart logPart2 = mock(LogPart.class);
    given(logPart2.isUpToDate()).willReturn(false);

    DataSet instance = new DataSet("name", Lists.newArrayList(logPart1, logPart2));

    instance.run();

    InOrder inOrder = inOrder(logPart1, logPart2);
    inOrder.verify(logPart1).execute();
    inOrder.verify(logPart2).execute();
  }
}
