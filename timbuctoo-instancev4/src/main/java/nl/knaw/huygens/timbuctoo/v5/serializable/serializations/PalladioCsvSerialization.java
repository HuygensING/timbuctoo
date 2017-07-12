package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.Edge;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.EntityFirstSerialization;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Flat representation of the graph as <code>csv</code>. This serialization represents the graph as a table
 * with each relation as a row. The columns of these rows are
 * <pre>
 *   source_id, {source leaf-fields}, target_id, {target leaf-fields}, relation
 * </pre>
 * The id's are generated values. Multiple values for one leaf-field are overwritten.
 * This serialization can be used in <a href="http://hdlab.stanford.edu/palladio/">Palladio</a>.
 * <p>
 *   Loose entities are not rendered: are they source entities or target entities?
 * </p>
 */
public class PalladioCsvSerialization extends EntityFirstSerialization {

  private final CSVPrinter csvPrinter;
  private List<String> columns;

  public PalladioCsvSerialization(OutputStream outputStream) throws IOException {
    csvPrinter = new CSVPrinter(new PrintWriter(outputStream), CSVFormat.EXCEL);
  }

  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    super.initialize(tocGenerator, typeNameStore);
    columns = getLeafFieldNames();
    csvPrinter.print("s_id");
    for (String column : columns) {
      csvPrinter.print("s_" + getTypeNameStore().makeGraphQlname(column));
    }
    csvPrinter.print("t_id");
    for (String column : columns) {
      csvPrinter.print("t_" + getTypeNameStore().makeGraphQlname(column));
    }
    csvPrinter.print("relation");
    csvPrinter.println();
  }

  @Override
  public void onDeclaredEntityEdge(Edge edge) throws IOException {
    if (edge.isNodeEdge()) {
      List<String> sourceFields = new ArrayList<>(Collections.nCopies(columns.size(), ""));
      List<String> targetFields = new ArrayList<>(Collections.nCopies(columns.size(), ""));
      for (Edge outEdge : edge.getSourceEntity().getOutEdges()) {
        int index = columns.indexOf(outEdge.getName());
        if (index > -1) {
          sourceFields.set(index, outEdge.getTargetAsString());
        }
      }
      for (Edge outEdge : edge.getTargetEntity().getOutEdges()) {
        int index = columns.indexOf(outEdge.getName());
        if (index > -1) {
          targetFields.set(index, outEdge.getTargetAsString());
        }
      }
      csvPrinter.print(edge.getSourceEntity().getId());
      for (String sourceField : sourceFields) {
        csvPrinter.print(sourceField);
      }
      csvPrinter.print(edge.getTargetEntity().getId());
      for (String v : targetFields) {
        csvPrinter.print(v);
      }
      csvPrinter.print(getTypeNameStore().makeGraphQlname(edge.getName()));
      csvPrinter.println();
    }
  }

  @Override
  public void finish() throws IOException {
    csvPrinter.flush();
    csvPrinter.close();
  }
}
