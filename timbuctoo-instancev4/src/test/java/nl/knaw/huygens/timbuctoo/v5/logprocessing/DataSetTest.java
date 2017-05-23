package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import org.junit.Test;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.OptionalPresentMatcher.present;
import static nl.knaw.huygens.timbuctoo.v5.logprocessing.RdfLogEntryStubs.logPartNotUpToDate;
import static nl.knaw.huygens.timbuctoo.v5.logprocessing.RdfLogEntryStubs.logPartUpToDate;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
    RdfLogEntry rdfLogEntry = mock(RdfLogEntry.class);
    given(rdfLogEntry.isUpToDate()).willReturn(false);
    instance.addLogPart(rdfLogEntry);

    boolean upToDate = instance.isUpToDate();

    assertThat(upToDate, is(false));
  }

  @Test
  public void nextLogToProcessReturnsTheNextNonUpToDateTaskOfTheDataSet() {
    DataSet instance = new DataSet("name");
    RdfLogEntry rdfLogEntryUpToDate = logPartUpToDate();
    instance.addLogPart(rdfLogEntryUpToDate);
    RdfLogEntry rdfLogEntryNotUpToDate = logPartNotUpToDate();
    instance.addLogPart(rdfLogEntryNotUpToDate);

    Optional<RdfLogEntry> logPart = instance.nextLogToProcess();

    assertThat(logPart, is(present()));
    assertThat(logPart.get(), is(sameInstance(rdfLogEntryNotUpToDate)));
  }

  @Test
  public void nextLogToProcessReturnsAnEmptyOptionalWhenAllTheLogsAreUpToDate() {
    DataSet instance = new DataSet("name");
    RdfLogEntry rdfLogEntryUpToDate = logPartUpToDate();
    instance.addLogPart(rdfLogEntryUpToDate);

    Optional<RdfLogEntry> logPart = instance.nextLogToProcess();

    assertThat(logPart, is(not(present())));
  }

}
