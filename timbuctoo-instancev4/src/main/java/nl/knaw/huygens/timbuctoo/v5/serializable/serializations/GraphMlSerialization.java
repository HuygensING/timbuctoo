package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Value;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.CollectionsOfEntitiesSerialization;

public class GraphMlSerialization extends CollectionsOfEntitiesSerialization {

  //  private final CSVPrinter csvPrinter;
  private String tableName;
  private String columnHeaders;
  private List<String> columns;
  private int count = 0;
  private int countEdge = 0;

  public GraphMlSerialization(OutputStream outputStream) throws IOException {
    //    csvPrinter = new CSVPrinter(new PrintWriter(outputStream), CSVFormat.EXCEL);
  }

  protected void initialize(List<String> columnHeaders) throws IOException {
    writeHeader();
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

  protected void writeHeader() {
    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n" +  
      "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
      "    xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n" +
      "        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">";
    System.out.println(header);
    System.out.println("  <graph id=\"G\" edgedefault=\"undirected\">");
  }

  protected void writeFooter() {
    System.out.println("  </graph>");
    String footer = "</graphml>";
    System.out.println(footer);
  }

  protected void writeRow(List<Value> values) throws IOException {
    System.out.println("    <node id=\"n" + count + "\">");
    for (Value value: values) {
      System.out.println("      <data key=\"" + value.getType() + "\">" + value.getValue() + "</data>");
    }
    System.out.println("    </node>");
    count++;
  }
  
  protected void writeEdge(String source, String target) throws IOException {
    System.out.println("    <edge id=\"e" + countEdge  + "\" source=\"" +
      source  + "\" target=\"" + target + "\"/>");
    countEdge++;
  }
}
