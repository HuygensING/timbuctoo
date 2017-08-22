package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Value;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.CollectionsOfEntitiesSerialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SqlSerialization extends CollectionsOfEntitiesSerialization {

  //  private final CSVPrinter csvPrinter;
  private String tableName;
  private String columnHeaders;
  private String createTable;
  private List<Tuple<String, String>> columns; 

  public SqlSerialization(OutputStream outputStream) throws IOException {
    //    csvPrinter = new CSVPrinter(new PrintWriter(outputStream), CSVFormat.EXCEL);
  }

  protected void initialize(List<String> columnHeaders) throws IOException {
    this.columnHeaders = "";
    createTable = "CREATE TABLE tableName (";
    columns = new ArrayList<Tuple<String,String>>();
    for (String columnHeader : columnHeaders) {
      Tuple<String,String> columnTuple = new Tuple<String, String>(columnHeader, "text");
      columns.add(columnTuple);
      this.columnHeaders += ", " + columnHeader;
      createTable += columnHeader + " text, ";
    }
    this.columnHeaders = this.columnHeaders.substring(2);
    createTable = createTable.subSequence(0, createTable.length() - 2) + ");";
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
    for (Tuple<String,String> column: columns) {
      String columnName = column.getLeft();
      String columnType = column.getRight();
      createTableString += columnName + " " + columnType + ",\n";
    }
    createTableString += ");";
    System.out.println(createTableString);
  }

  protected void writeRow(List<Value> values) throws IOException {
    String columnValues = "";
    for (Value value : values) {
      if (value == null) {
        columnValues += ", DEFAULT";
      } else {
        columnValues += ", '" + value.getValue().toString() + "'";
      }
    }
    System.out.println("INSERT INTO " + this.tableName + "(" + columnHeaders +
            ") VALUES (" + columnValues.substring(2) + ");");
  }
  
  protected void replaceColumnType(String columnName, String columnType) {
    Tuple<String,String> newTuple = new Tuple<String,String>(columnName, columnType);
    int counter = 0;
    for (Tuple<String,String> column: columns) {
      if (column.getLeft().equals(columnName)) {
        break;
      }
      counter++;
    }
    if (counter < columns.size()) {
      columns.set(counter, newTuple);
    }
  }

}
