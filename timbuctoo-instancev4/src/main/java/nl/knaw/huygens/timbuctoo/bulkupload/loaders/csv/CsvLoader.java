package nl.knaw.huygens.timbuctoo.bulkupload.loaders.csv;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

/**
 * A CsvLoader parses a CSV file containing a single collection.
 * The default format is Excel's CSV format, with a header row.
 */
public class CsvLoader implements Loader {
  private final CSVFormat format;
  private String collectionName;

  public CsvLoader(String collectionName) {
    this(CSVFormat.EXCEL.withHeader(), collectionName);
  }

  /**
   * Constructs a CsvLoader. This only sets options.
   *
   * @param format         CSV format.
   * @param collectionName Name of the collection described in the CSV file.
   */
  public CsvLoader(CSVFormat format, String collectionName) {
    format = Objects.requireNonNull(format);
    if (format.getHeader() == null) {
      throw new IllegalArgumentException("CSV format must include header; use withHeader() to parse from file");
    }
    this.format = format;
    this.collectionName = Objects.requireNonNull(collectionName);
  }

  @Override
  public void loadData(File file, Importer importer) throws InvalidFileException, IOException {
    CSVParser parser = format.parse(new FileReader(file));

    importer.startCollection(collectionName);

    parser.getHeaderMap().forEach((name, column) -> importer.registerPropertyName(column, name));

    parser.forEach(row -> {
      importer.startEntity();
      for (int i = 0; i < row.size(); i++) {
        importer.setValue(i, row.get(i));
      }
      importer.finishEntity();
    });

    importer.finishCollection();
  }
}
