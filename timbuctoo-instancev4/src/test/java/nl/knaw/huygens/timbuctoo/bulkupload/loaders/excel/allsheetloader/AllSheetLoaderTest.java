package nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.allsheetloader;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImporterStubs;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AllSheetLoaderTest {

  @Test
  public void importExcel() throws Exception {
    URL excelFileUrl = AllSheetLoaderTest.class.getResource("test.xlsx");
    File excelFile = new File(excelFileUrl.toURI());

    AllSheetLoader instance = new AllSheetLoader();
    try {
      List<String> results = new ArrayList<>();
      AtomicBoolean failure = new AtomicBoolean(false);
      instance.loadData(excelFile, ImporterStubs.withCustomReporter(logline -> {
        if (logline.matches("failure.*")) {
          failure.set(true);
        }
        results.add(logline);
      }));
      if (failure.get()) {
        throw new RuntimeException("Failure during import: \n" + String.join("\n", results));
      }
    } catch (InvalidFileException e) {
      throw new RuntimeException(e);
    }
  }
}
