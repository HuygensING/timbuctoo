package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.TimeWithUnit;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created on 2017-12-10 15:51.
 */
public class DataSetImportStatusTest {

  @Test
  public void lastImportDurationNotSet() {
    LogList logList = new LogList();
    DataSetImportStatus dis = new DataSetImportStatus(logList);

    long duration = dis.getLastImportDuration(TimeUnit.SECONDS.name());
    assertThat(duration, is(-1L));
  }

  @Test
  public void lastImportDurationConvertFromSecondsToMilliseconds() {
    LogList logList = new LogList();
    DataSetImportStatus dis = new DataSetImportStatus(logList);
    logList.setLastImportDuration(new TimeWithUnit().withSeconds(123));

    long duration = dis.getLastImportDuration(TimeUnit.MILLISECONDS.name());
    assertThat(duration, is(123000L));
  }

  @Test
  public void lastImportDurationConvertFromMilliSecondsToSeconds() {
    LogList logList = new LogList();
    DataSetImportStatus dis = new DataSetImportStatus(logList);
    logList.setLastImportDuration(new TimeWithUnit().withMilliseconds(123456));

    long duration = dis.getLastImportDuration(TimeUnit.SECONDS.name());
    assertThat(duration, is(123L));
  }
}
