package nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.allsheetloader;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImporterStubs;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.io.ByteStreams.toByteArray;

public class AllSheetLoaderTest {

  @Test
  public void importExcel() {
    InputStream excelFile = AllSheetLoaderTest.class.getResourceAsStream("test.xlsx");

    AllSheetLoader instance = new AllSheetLoader();
    try {
      List<String> results = new ArrayList<>();
      AtomicBoolean failure = new AtomicBoolean(false);
      instance.loadData(toByteArray(excelFile), ImporterStubs.withCustomReporter(logline -> {
        if (logline.matches("failure.*")) {
          failure.set(true);
        }
        results.add(logline);
      }));
      if (failure.get()) {
        throw new RuntimeException("Failure during import: \n" + String.join("\n", results));
      }
    } catch (InvalidFileException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
