package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.ResultToC;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serialization;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

public class CsvSerialization implements Serialization {

  private final CSVPrinter csvPrinter;
  private boolean writtenValue;

  public CsvSerialization(OutputStream outputStream) throws IOException {
    csvPrinter = new CSVPrinter(new PrintWriter(outputStream), CSVFormat.EXCEL);

  }

  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    ResultToC resultToC = tocGenerator.generateToC();
    printHeader(resultToC, "");
    csvPrinter.println();
  }

  private void printHeader(ResultToC resultToC, String key) throws IOException {
    if (resultToC.getFields().isEmpty()) {
      if (resultToC.getMaxCount() > 0) { //print array items toc
        for (int i = 0; i < resultToC.getMaxCount(); i++) {
          csvPrinter.print(key + "." + i);
        }
      } else {
        csvPrinter.print(key);
      }
      return;
    }

    for (Map.Entry<String, ResultToC> stringResultToCEntry : resultToC.getFields().entrySet()) {
      printHeader(stringResultToCEntry.getValue(), key + "." + stringResultToCEntry.getKey());
    }

  }

  @Override
  public void finish() throws IOException {
    csvPrinter.flush();
    csvPrinter.close();
  }

  @Override
  public void onStartEntity(String uri) throws IOException {
    // nothing special to do
  }

  @Override
  public void onProperty(String propertyName) throws IOException {
    // nothing special to do
  }

  @Override
  public void onCloseEntity(String uri) throws IOException {
    if (writtenValue) {
      csvPrinter.println();
      writtenValue = false;
    }
  }

  @Override
  public void onStartList() throws IOException {
    // nothing special to do
  }

  @Override
  public void onListItem(int index) throws IOException {
    // nothing special to do
  }

  @Override
  public void onCloseList() throws IOException {
    // nothing special to do
  }

  @Override
  public void onRdfValue(Object value, String valueType) throws IOException {
    writeValue(value);
  }

  private void writeValue(Object value) throws IOException {
    writtenValue = true;
    csvPrinter.print(value);
  }

  @Override
  public void onValue(Object value) throws IOException {
    writeValue(value);
  }
}
