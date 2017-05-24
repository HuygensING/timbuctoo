package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import com.google.common.io.Files;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.dto.LocalLog;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(Parameterized.class)
public class DataSetTest {
  private final DataSet dataSet;
  private final Runnable cleaner;
  private final DataSetProcessor processor;

  public DataSetTest(Function<DataSetProcessor, DataSet> dataSet, Runnable cleaner, String className) {
    this.processor = mock(DataSetProcessor.class);
    this.dataSet = dataSet.apply(processor);
    this.cleaner = cleaner;
  }

  @After
  public void cleanUp() {
    cleaner.run();
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



  @Parameterized.Parameters(name = "{1}")
  public static Collection<Object[]> instancesToTest() {
    File tempDir = Files.createTempDir();
    return Lists.<Object[]>newArrayList(
      new Object[]{
        (Function<DataSetProcessor, DataSet>) processor -> new FileSystemBasedDataSet(tempDir, processor),
        (Runnable) () -> tempDir.delete(),
        FileSystemBasedDataSet.class.getName()
      }
    );
  }
}
