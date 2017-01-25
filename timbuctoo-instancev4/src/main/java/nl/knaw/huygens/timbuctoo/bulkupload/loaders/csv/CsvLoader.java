package nl.knaw.huygens.timbuctoo.bulkupload.loaders.csv;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * A CsvLoader parses CSV files and treats each file as a separate collection.
 * The default format is Excel's CSV format, with a header row.
 */
public class CsvLoader implements Loader {
  private final CSVFormat format;

  public CsvLoader() {
    this(CSVFormat.EXCEL.withHeader());
  }

  /**
   * Constructs a CsvLoader. This only sets options.
   *
   * @param format         CSV format.
   */
  public CsvLoader(CSVFormat format) {
    format = Objects.requireNonNull(format);
    if (format.getHeader() == null) {
      throw new IllegalArgumentException("CSV format must include header; use withHeader() to parse from file");
    }
    this.format = format;
  }

  @Override
  public void loadData(List<Tuple<String, File>> files, Importer importer) throws InvalidFileException, IOException {
    for (Tuple<String, File> file : files) {
      CSVParser parser = format.parse(new FileReader(file.getRight()));

      String filename = file.getLeft();
      //remove well-known extensions
      if (filename.endsWith(".csv") || filename.endsWith(".tsv") || filename.endsWith(".txt")) {
        filename = filename.substring(filename.length() - 1);
      }
      importer.startCollection(filename);

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
}
