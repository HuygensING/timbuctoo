package nl.knaw.huygens.timbuctoo.serializable.serializations;

import nl.knaw.huygens.timbuctoo.serializable.serializations.base.FlatTableSerialization;
import nl.knaw.huygens.timbuctoo.serializable.dto.Value;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

public class CsvSerialization extends FlatTableSerialization {

  private final CSVPrinter csvPrinter;

  public CsvSerialization(OutputStream outputStream) throws IOException {
    csvPrinter = new CSVPrinter(new PrintWriter(outputStream), CSVFormat.EXCEL);
  }

  @Override
  protected void initialize(List<String> columnHeaders) throws IOException {
    for (String columnHeader : columnHeaders) {
      csvPrinter.print(columnHeader);
    }

    csvPrinter.println();
  }

  @Override
  protected void writeRow(List<Value> values) throws IOException {
    for (Value value : values) {
      if (value == null) {
        csvPrinter.print(null);
      } else {
        csvPrinter.print(value.getValue());
      }
    }
    csvPrinter.println();
  }

  @Override
  protected void finish() throws IOException {
    csvPrinter.flush();
    csvPrinter.close();
  }

}
