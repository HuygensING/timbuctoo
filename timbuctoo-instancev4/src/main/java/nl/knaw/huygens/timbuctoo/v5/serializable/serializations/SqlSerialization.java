package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Value;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.FlatTableSerialization;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

public class SqlSerialization extends FlatTableSerialization {

  private final CSVPrinter csvPrinter;
  private String tableName;
  private String columnHeaders;
  private String createTable;

  public SqlSerialization(OutputStream outputStream) throws IOException {
    csvPrinter = new CSVPrinter(new PrintWriter(outputStream), CSVFormat.EXCEL);
  }

  @Override
  protected void initialize(List<String> columnHeaders) throws IOException {
    //    for (String columnHeader : columnHeaders) {
    //      csvPrinter.print(columnHeader);
    //    }
    this.columnHeaders = "";
    createTable = "CREATE TABLE tableName (";
    for (String columnHeader : columnHeaders) {
      this.columnHeaders += ", " + columnHeader;
      createTable += columnHeader + " text,";
    }
    this.columnHeaders = this.columnHeaders.substring(2);
    setTableName("");
    createTable += createTable.subSequence(0, createTable.length() - 1) + ")";
  }
  
  protected void setTableName(String tableName) {
    this.tableName = tableName;
    createTable.replace("tableName", tableName);
    System.out.println("DROP TABLE " + tableName + ";");
    System.out.println(createTable);
  }

  @Override
  protected void writeRow(List<Value> values) throws IOException {
    String columnValues = "";
    for (Value value : values) {
      if (value == null) {
        columnValues += ", DEFAULT";
      } else {
        columnValues += ", '" + value.getValue().toString() + "'";
      }
    }
    System.out.println("INSERT INTO " + tableName + "(" + columnHeaders +
            ") VALUES (" + columnValues.substring(2) + ");");
  }

  @Override
  protected void finish() throws IOException {
  }

}
