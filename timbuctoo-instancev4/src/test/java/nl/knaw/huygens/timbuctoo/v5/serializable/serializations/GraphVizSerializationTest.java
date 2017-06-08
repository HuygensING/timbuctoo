package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
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
    OutputStream out = new ByteArrayOutputStream();
    GraphVizSerialization gs = new GraphVizSerialization(out);
    SerializableObject graph = createGraph_01(createTypeNameStore());

    graph.performSerialization(gs);
    String result = out.toString();
    //System.out.println(result);
    String expected = "digraph {\n" +
      "\t\"uri0\" -> \"uri1\" [label=\"hasBeer\"];\n" +
      "\t\"uri1\" -> \"uri2\" [label=\"hasChild\"];\n" +
      "\t\"uri2\" -> \"uri3\" [label=\"hasSibling\"];\n" +
      "\t\"uri3\" -> \"uri4\" [label=\"fooBar\"];\n" +
      "\t\"uri1\" -> \"uri3\" [label=\"hasChild\"];\n" +
      "}\n";
    assertThat(result, equalTo(expected));
  }
}
