package nl.knaw.huygens.timbuctoo.v5.dataset;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.fail;

@Ignore
public class DataSetTest {

  @Test
  public void addLogSavesTheLogToDisk() throws Exception {
    fail("not yet implemented");
  }

  @Test
  public void generateLogSavesTheLogToDisk() throws Exception {
    fail("not yet implemented");
  }


  @Test
  public void callsStoresWhenANewLogIsAdded() {
    //zijn eigenlijk 4 tests:

    // - calls tripleStore (whenever)
    // - calls collectionIndex (whenever)
    // - calls prefixStore (whenever)
    // - calls SchemaStore after tripleStore finishes
  }

  @Test
  public void generateLogCallsTheStores() {
    //check if the addLog method is called
    //of checken of de stores op dezelfde manier worden aangeroepen
  }



}
