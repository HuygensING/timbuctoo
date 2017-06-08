package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.DistinctSerialization;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.Edge;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Created on 2017-06-07 16:16.
 */
public class GraphVizSerialization extends DistinctSerialization {

  private final PrintStream printStream;

  public GraphVizSerialization(OutputStream outputStream) throws IOException {
    printStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8.name());
  }

  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    printStream.println("digraph {");
  }

  @Override
  public void finish() throws IOException {
    printStream.println("}");
    printStream.close();
  }

  @Override
  public void onDistinctEdge(Edge edge) throws IOException {
    if (edge.isNodeEdge()) {
      String line = "\t\"" + edge.getSourceUri() + "\" -> \"" + edge.getTargetUri() +
        "\" [label=\"" + edge.getName() + "\"];";
      printStream.println(line);
    }
  }
}
