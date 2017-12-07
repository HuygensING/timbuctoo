package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

/**
 * Created on 2017-12-07 15:09.
 */
public class ImportStatusTest {

  @Test
  public void serialize() throws Exception {
    ImportStatus eis = new ImportStatus();
    eis.setElapsedTimeMillis(123L);

    String result = new ObjectMapper().writeValueAsString(eis);
    //System.out.println(result);
  }
}
