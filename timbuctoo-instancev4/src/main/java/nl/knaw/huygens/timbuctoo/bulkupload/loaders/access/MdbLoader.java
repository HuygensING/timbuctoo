package nl.knaw.huygens.timbuctoo.bulkupload.loaders.access;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.QuoteMode;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An MdbLoader parses mdb files and treats each table as a separate collection.
 */
public class MdbLoader implements Loader {

  public MdbLoader() {
  }

  @Override
  public void loadData(List<Tuple<String, File>> files, Importer importer) throws InvalidFileException, IOException {
    for (Tuple<String, File> file : files) {

      String filename = file.getLeft();
      //remove well-known extensions
      filename = filename.substring(0, filename.length() - 4);

      Table table;
      try {
        File input = file.getRight();
        System.out.println("input: " + input.getAbsolutePath());
        Database database = DatabaseBuilder.open(input);
        Set<String> tableNames = database.getTableNames();
        for (String tableName : tableNames) {
          System.out.println("tableName: " + tableName);
          importer.startCollection(tableName);
          table = database.getTable(tableName);
          List<? extends Column> columns = table.getColumns();
          String allColumns = "";
          for (int i = 0; i < columns.size(); i++) {
            //          for (Column column : columns) {
            importer.registerPropertyName(i, columns.get(i).getName());
            allColumns += " " + columns.get(i).getName();
          }
          System.out.println("  all columns: " + allColumns);

          int rowNum = 0;
          for (Row row : table) {
            importer.startEntity();
            for (int colNum = 0 ; colNum < columns.size(); colNum++) {
              //for (Column column : columns) {
              Object cellValue = row.get(columns.get(colNum).getName());
              //              if (rowValue != null) {
              if (cellValue == null) {
                cellValue = "";
              }
              importer.setValue(colNum, "" + cellValue);
              // System.out.println("Column '" + column.getName() + "' : " + rowValue);
              //              }
            }
            rowNum++;
            importer.finishEntity();
          }
          System.out.println("number of rows: " + rowNum);
          importer.finishCollection();
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }





  }
}
