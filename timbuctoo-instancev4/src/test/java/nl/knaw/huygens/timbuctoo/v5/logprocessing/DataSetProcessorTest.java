package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import org.assertj.core.util.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.v5.logprocessing.DataSetStubs.dataSetWithLogs;
import static nl.knaw.huygens.timbuctoo.v5.logprocessing.LogPartStubs.logPartNotUpToDate;
import static org.hamcrest.MatcherAssert.assertThat;

public class DataSetProcessorTest {

  @Test
  public void runExecutesEachLogPartOfTheDataSet() {
    LogPart logPart1 = logPartNotUpToDate();
    LogPart logPart2 = logPartNotUpToDate();
    DataSet dataSet = dataSetWithLogs(Lists.newArrayList(logPart1, logPart2));
    DataSetProcessor instance = new DataSetProcessor(dataSet);
    assertThat(logPart1.isUpToDate(), CoreMatchers.is(false));
    assertThat(logPart2.isUpToDate(), CoreMatchers.is(false));

    instance.run();

    assertThat(logPart1.isUpToDate(), CoreMatchers.is(true));
    assertThat(logPart2.isUpToDate(), CoreMatchers.is(true));
  }

}
