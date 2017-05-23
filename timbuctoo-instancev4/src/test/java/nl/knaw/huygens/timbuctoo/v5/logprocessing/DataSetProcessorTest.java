package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import org.assertj.core.util.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.v5.logprocessing.DataSetStubs.dataSetWithLogs;
import static nl.knaw.huygens.timbuctoo.v5.logprocessing.RdfLogEntryStubs.logPartNotUpToDate;
import static org.hamcrest.MatcherAssert.assertThat;

public class DataSetProcessorTest {

  @Test
  public void runExecutesEachLogPartOfTheDataSet() {
    RdfLogEntry rdfLogEntry1 = logPartNotUpToDate();
    RdfLogEntry rdfLogEntry2 = logPartNotUpToDate();
    DataSet dataSet = dataSetWithLogs(Lists.newArrayList(rdfLogEntry1, rdfLogEntry2));
    DataSetProcessor instance = new DataSetProcessor(dataSet);
    assertThat(rdfLogEntry1.isUpToDate(), CoreMatchers.is(false));
    assertThat(rdfLogEntry2.isUpToDate(), CoreMatchers.is(false));

    instance.run();

    assertThat(rdfLogEntry1.isUpToDate(), CoreMatchers.is(true));
    assertThat(rdfLogEntry2.isUpToDate(), CoreMatchers.is(true));
  }

}
