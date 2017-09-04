package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Value;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.CollectionsOfEntitiesSerialization;

public class SqlSerialization extends CollectionsOfEntitiesSerialization {

  //  private final CSVPrinter csvPrinter;
  private String tableName;
  private String columnHeaders;
  private List<String> columns;

  public SqlSerialization(OutputStream outputStream) throws IOException {
    //    csvPrinter = new CSVPrinter(new PrintWriter(outputStream), CSVFormat.EXCEL);
  }

  protected void initialize(List<String> columnHeaders) throws IOException {
    this.columnHeaders = "";
    columns = new ArrayList<String>();
    for (String columnHeader : columnHeaders) {
      columns.add(columnHeader);
      this.columnHeaders += ", " + columnHeader;
      this.columnHeaders += ", " + columnHeader + "_type";
    }
    this.columnHeaders = this.columnHeaders.substring(2);
  }
  
  protected void setTableName(String tableName) {
    this.tableName = tableName;
  }
  
  protected void writeCreateTable() {
    if (tableName.isEmpty()) {
      System.err.println("writeCreateTable: tableName is empty!");
      tableName = "tableName";
    }
    System.out.println("DROP TABLE " + tableName + ";");
    String createTableString = "CREATE TABLE " + tableName + " (\n";
    for (String column: columns) {
      createTableString += column + " text,\n";
      createTableString += column + "_type text,\n";
    }
    createTableString = createTableString.substring(0, createTableString.length() - 2);
    createTableString += ");";
    System.out.println(createTableString);
  }

  protected void writeRow(List<Value> values) throws IOException {
    String columnValues = "";
    for (Value value : values) {
      if (value == null) {
        columnValues += ", DEFAULT";
        columnValues += ", 'text'";
      } else {
        columnValues += ", '" + value.getValue().toString() + "'";
        columnValues += ", '" + value.getType() + "'";
      }
    }
    System.out.println("INSERT INTO " + this.tableName + "(" + columnHeaders +
            ") VALUES (" + columnValues.substring(2) + ");");
  }

}
