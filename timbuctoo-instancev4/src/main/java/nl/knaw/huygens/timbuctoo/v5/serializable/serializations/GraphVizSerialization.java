package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.DistinctSerialization;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.Edge;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.Entity;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.EntityFirstSerialization;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Created on 2017-06-07 16:16.
 */
public class GraphVizSerialization extends EntityFirstSerialization {

  private final PrintStream printStream;

  public GraphVizSerialization(OutputStream outputStream) throws IOException {
    printStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8.name());
  }

  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    super.initialize(tocGenerator, typeNameStore);
    printStream.println("digraph {");
  }

  @Override
  public void finish() throws IOException {
    printStream.println("}");
    printStream.close();
  }

  @Override
  public void onDistinctEntity(Entity entity) throws IOException {
    String shortUri = getTypeNameStore().shorten(entity.getUri());
    // a [style=filled, fillcolor=red]; could call a lambda function entity -> return list of key=value pairs
    String line = "\t\"" + shortUri + "\";";
    printStream.println(line);
    super.onDistinctEntity(entity);

  }

  @Override
  public void onDeclaredEntityEdge(Edge edge) throws IOException {
    if (edge.isNodeEdge()) {
      String propertyName = getTypeNameStore().makeGraphQlname(edge.getName());
      String shortSource = getTypeNameStore().shorten(edge.getSourceUri());
      String shortTarget = getTypeNameStore().shorten(edge.getTargetUri());
      String line = "\t\"" + shortSource + "\" -> \"" + shortTarget +
        "\" [label=\"" + propertyName + "\"];";
      printStream.println(line);
    }
  }

}
