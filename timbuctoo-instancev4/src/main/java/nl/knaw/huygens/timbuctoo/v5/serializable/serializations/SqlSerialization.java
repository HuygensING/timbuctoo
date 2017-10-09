package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Value;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.FlatTableSerialization;

public class SqlSerialization extends FlatTableSerialization {

  private String tableName = "graphqlexport";
  private String columnHeaders;
  private List<String> columns;
  protected final PrintWriter writer;
  protected boolean firstLine = true;
  private int id = 0;

  public SqlSerialization(OutputStream outputStream) throws IOException {
    writer = new PrintWriter(outputStream);
  }

  protected void initialize(List<String> columnHeaders) throws IOException {
    this.columnHeaders = "id";
    columns = new ArrayList<String>();
    for (String columnHeader : columnHeaders) {
      columnHeader = columnHeader.replaceAll("\\.", "_");
      columns.add(columnHeader);
      this.columnHeaders += ", " + columnHeader;
      this.columnHeaders += ", " + columnHeader + "_type";
    }
    // this.columnHeaders = this.columnHeaders.substring(2);
  }
  
  protected void setTableName(String tableName) {
    this.tableName = tableName;
  }
  
  protected void writeCreateTable() {
    if (tableName.isEmpty()) {
      // System.err.println("writeCreateTable: tableName is empty!");
      tableName = "tableName";
    }
    writer.println("DROP TABLE " + tableName + ";");
    String createTableString = "CREATE TABLE " + tableName + " (\n";
    createTableString += "id integer UNIQUE NOT NULL,\n";
    for (String column: columns) {
      createTableString += column + " text,\n";
      createTableString += column + "_type text,\n";
    }
    createTableString = createTableString.substring(0, createTableString.length() - 2);
    createTableString += ");";
    writer.println(createTableString);
  }

  protected void writeRow(List<Value> values) throws IOException {
    if (firstLine) {
      writeCreateTable();
      firstLine = false;
    }
    id++;
    String columnValues = id + "";
    for (Value value : values) {
      if (value == null) {
        columnValues += ", DEFAULT";
        columnValues += ", 'text'";
      } else {
        columnValues += ", '" + value.getValue().toString() + "'";
        columnValues += ", '" + value.getType() + "'";
      }
    }
    writer.println("INSERT INTO " + this.tableName + " (" + columnHeaders +
            ") VALUES (" + columnValues + ");");
  }
  
  @Override
  protected void finish() throws IOException {
    writer.flush();
    writer.close();
  }

}
