package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.SerializationTest;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created on 2017-06-08 08:42.
 */
public class GraphVizSerializationTest extends SerializationTest {

  @Test
  public void performSerialization() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    GraphVizSerialization gs = new GraphVizSerialization(out);
    SerializableObject graph = createGraph_01(createTypeNameStore());

    graph.performSerialization(gs);
    String result = out.toString();
    //System.out.println(result);
    saveAs(out,"graphviz_02.gv");

    String expected = "digraph {\n" +
      "\t\"uri4\";\n" +
      "\t\"uri3\";\n" +
      "\t\"uri3\" -> \"uri4\" [label=\"fooBar\"];\n" +
      "\t\"uri2\";\n" +
      "\t\"uri2\" -> \"uri3\" [label=\"hasSibling\"];\n" +
      "\t\"uri1\";\n" +
      "\t\"uri1\" -> \"uri3\" [label=\"hasChild\"];\n" +
      "\t\"uri1\" -> \"uri2\" [label=\"hasChild\"];\n" +
      "\t\"uri0\";\n" +
      "\t\"uri0\" -> \"uri1\" [label=\"hasBeer\"];\n" +
      "}\n";
    assertThat(result, equalTo(expected));
  }
}
