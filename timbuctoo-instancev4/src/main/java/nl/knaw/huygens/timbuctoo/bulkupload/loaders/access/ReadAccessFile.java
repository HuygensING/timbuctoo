package nl.knaw.huygens.timbuctoo.bulkupload.loaders.access;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

public class ReadAccessFile {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    Table table;
    try {
      File input = new File("C:/Project Timbuctoo (2)/regent-t_16-18Friesland.mdb");
      System.out.println("input: " + input.getAbsolutePath());
      Database database = DatabaseBuilder.open(input);
      Set<String> tableNames = database.getTableNames();
      for (String tableName : tableNames) {
        System.out.println("tableName: " + tableName);
        table = database.getTable(tableName);
        List<? extends Column> columns = table.getColumns();
        for (Column column : columns) {
          System.out.println("  column: " + column.getName());
        }
        for (Row row : table) {
          for (Column column : columns) {
            Object rowValue = row.get(column.getName());
            if (rowValue != null) {
              System.out.println("Column '" + column.getName() + "' : " + rowValue);
            }
          }
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
