package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.Edge;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.EntityFirstSerialization;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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
 *
 * This serialization can be used in <a href="http://hdlab.stanford.edu/palladio/">Palladio</a>.
 */
public class CsvSerialization extends EntityFirstSerialization {

  private final PrintStream printStream;
  private List<String> columns;

  public CsvSerialization(OutputStream outputStream) throws IOException {
    printStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8.name());
  }

  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    super.initialize(tocGenerator, typeNameStore);
    columns = getLeafFieldNames();
    printStream.print("s_id,");
    columns.forEach(f -> printStream.print("s_" + f + ","));
    printStream.print("t_id,");
    columns.forEach(f -> printStream.print("t_" + f + ","));
    printStream.println("relation");
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
      printStream.print(edge.getSourceEntity().getId() + ",");
      sourceFields.forEach(v -> printStream.print(v + ","));
      printStream.print(edge.getTargetEntity().getId() + ",");
      targetFields.forEach(v -> printStream.print(v + ","));
      printStream.println(edge.getName());
    }
  }
}
