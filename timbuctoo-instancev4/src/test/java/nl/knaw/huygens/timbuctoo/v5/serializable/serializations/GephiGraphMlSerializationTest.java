package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.SerializationTest;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created on 2017-06-08 08:49.
 */
public class GephiGraphMlSerializationTest extends SerializationTest {

  @Test
  public void performSerialization() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    SerializableObject graph = createGraph_01(createTypeNameStore());

    GephiGraphMlSerialization ggs = new GephiGraphMlSerialization(out);

    graph.performSerialization(ggs);
    String result = out.toString();

    //System.out.println(result);
    saveAs(out, "gephi_02.graphml");
    validate("http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd", result);

    String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n" +
      "  <key id=\"edgelabel\" attr.name=\"EdgeLabel\" attr.type=\"string\" for=\"edge\"></key>\n" +
      "  <key id=\"label\" attr.name=\"Label\" attr.type=\"string\" for=\"node\"></key>\n" +
      "  <key id=\"foo\" attr.name=\"foo\" attr.type=\"string\" for=\"node\"></key>\n" +
      "  <key id=\"name\" attr.name=\"name\" attr.type=\"string\" for=\"node\"></key>\n" +
      "  <key id=\"uri\" attr.name=\"uri\" attr.type=\"string\" for=\"node\"></key>\n" +
      "  <key id=\"items\" attr.name=\"items\" attr.type=\"string\" for=\"node\"></key>\n" +
      "  <graph edgedefault=\"directed\">\n" +
      "    <node id=\"uri4\">\n" +
      "      <data key=\"foo\">foo4</data>\n" +
      "      <data key=\"name\">name4</data>\n" +
      "      <data key=\"uri\">uri4</data>\n" +
      "    </node>\n" +
      "    <node id=\"uri3\">\n" +
      "      <data key=\"foo\">foo3</data>\n" +
      "      <data key=\"name\">name3</data>\n" +
      "      <data key=\"uri\">uri3</data>\n" +
      "    </node>\n" +
      "    <edge id=\"e20\" source=\"uri3\" target=\"uri4\">\n" +
      "      <data key=\"edgelabel\">fooBar</data>\n" +
      "    </edge>\n" +
      "    <node id=\"uri101\">\n" +
      "      <data key=\"foo\">foo101</data>\n" +
      "      <data key=\"name\">name101</data>\n" +
      "      <data key=\"uri\">uri101</data>\n" +
      "      <data key=\"items\">[multiple values]</data>\n" +
      "    </node>\n" +
      "    <node id=\"uri2\">\n" +
      "      <data key=\"foo\">foo2</data>\n" +
      "      <data key=\"name\">name2</data>\n" +
      "      <data key=\"uri\">uri2</data>\n" +
      "    </node>\n" +
      "    <edge id=\"e16\" source=\"uri2\" target=\"uri3\">\n" +
      "      <data key=\"edgelabel\">hasSibling</data>\n" +
      "    </edge>\n" +
      "    <edge id=\"e24\" source=\"uri2\" target=\"uri101\">\n" +
      "      <data key=\"edgelabel\">wroteBook</data>\n" +
      "    </edge>\n" +
      "    <node id=\"uri102\">\n" +
      "      <data key=\"foo\">foo102</data>\n" +
      "      <data key=\"name\">name102</data>\n" +
      "      <data key=\"uri\">uri102</data>\n" +
      "    </node>\n" +
      "    <edge id=\"e32\" source=\"uri102\" target=\"uri3\">\n" +
      "      <data key=\"edgelabel\">items</data>\n" +
      "    </edge>\n" +
      "    <edge id=\"e12\" source=\"uri102\" target=\"uri2\">\n" +
      "      <data key=\"edgelabel\">items</data>\n" +
      "    </edge>\n" +
      "    <node id=\"uri1\">\n" +
      "      <data key=\"foo\">foo1</data>\n" +
      "      <data key=\"name\">name1</data>\n" +
      "      <data key=\"uri\">uri1</data>\n" +
      "    </node>\n" +
      "    <edge id=\"e7\" source=\"uri1\" target=\"uri102\">\n" +
      "      <data key=\"edgelabel\">hasChild</data>\n" +
      "    </edge>\n" +
      "    <node id=\"uri0\">\n" +
      "      <data key=\"foo\">foo0</data>\n" +
      "      <data key=\"name\">name0</data>\n" +
      "      <data key=\"uri\">uri0</data>\n" +
      "    </node>\n" +
      "    <edge id=\"e3\" source=\"uri0\" target=\"uri1\">\n" +
      "      <data key=\"edgelabel\">hasBeer</data>\n" +
      "    </edge>\n" +
      "  </graph>\n" +
      "</graphml>";

    assertThat(result, equalTo(expected));
  }

}
