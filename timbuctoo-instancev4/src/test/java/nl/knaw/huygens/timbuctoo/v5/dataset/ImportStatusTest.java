package nl.knaw.huygens.timbuctoo.v5.dataset;


import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ImportStatusTest {

  @Test
  public void multipleStart() throws Exception {
    ImportStatus status = new ImportStatus();
    assertThat(status.isRunning(), is(false));
    status.setStarted("method", "baseUri");
    assertThat(status.isRunning(), is(true));

    status.setStarted("method2", "baseUri2");
    assertThat(status.isRunning(), is(true));

    status.setFinished();
    assertThat(status.isRunning(), is(false));
  }

}
