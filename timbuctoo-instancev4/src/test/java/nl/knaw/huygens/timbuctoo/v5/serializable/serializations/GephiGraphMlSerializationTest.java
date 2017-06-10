package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.SerializationTest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created on 2017-06-08 08:49.
 */
public class GephiGraphMlSerializationTest extends SerializationTest {

  @Test
  public void performSerialization() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    GephiGraphMlSerialization ggs = new GephiGraphMlSerialization(out);
    SerializableObject graph = createGraph_01(createTypeNameStore());

    graph.performSerialization(ggs);
    String result = out.toString();

    //System.out.println(result);
    //saveAs(out, "gephi_01.graphml");

    String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n" +
      "  <key id=\"edgelabel\" attr.name=\"EdgeLabel\" attr.type=\"string\" for=\"edge\"></key>\n" +
      "  <key id=\"label\" attr.name=\"Label\" attr.type=\"string\" for=\"node\"></key>\n" +
      "  <key id=\"wroteBook\" attr.name=\"wroteBook\" attr.type=\"string\" for=\"node\"></key>\n" +
      "  <key id=\"foo\" attr.name=\"foo\" attr.type=\"string\" for=\"node\"></key>\n" +
      "  <key id=\"name\" attr.name=\"name\" attr.type=\"string\" for=\"node\"></key>\n" +
      "  <key id=\"uri\" attr.name=\"uri\" attr.type=\"string\" for=\"node\"></key>\n" +
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
      "    <edge id=\"e16\" source=\"uri3\" target=\"uri4\">\n" +
      "      <data key=\"edgelabel\">fooBar</data>\n" +
      "    </edge>\n" +
      "    <node id=\"uri2\">\n" +
      "      <data key=\"foo\">foo2</data>\n" +
      "      <data key=\"name\">name2</data>\n" +
      "      <data key=\"uri\">uri2</data>\n" +
      "      <data key=\"wroteBook\">[multiple values]</data>\n" +
      "    </node>\n" +
      "    <edge id=\"e12\" source=\"uri2\" target=\"uri3\">\n" +
      "      <data key=\"edgelabel\">hasSibling</data>\n" +
      "    </edge>\n" +
      "    <node id=\"uri1\">\n" +
      "      <data key=\"foo\">foo1</data>\n" +
      "      <data key=\"name\">name1</data>\n" +
      "      <data key=\"uri\">uri1</data>\n" +
      "    </node>\n" +
      "    <edge id=\"e24\" source=\"uri1\" target=\"uri3\">\n" +
      "      <data key=\"edgelabel\">hasChild</data>\n" +
      "    </edge>\n" +
      "    <edge id=\"e8\" source=\"uri1\" target=\"uri2\">\n" +
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
    validate(result);
  }

  private void validate(String result) throws Exception {
    InputStream in = IOUtils.toInputStream(result, "UTF-8");
    Source source = new StreamSource(in);

    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = sf.newSchema(new URL("http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd"));

    Validator validator = schema.newValidator();
    validator.setErrorHandler(new GraphErrorHandler());
    validator.validate(source);
  }

  private static class GraphErrorHandler implements ErrorHandler {

    @Override
    public void warning(SAXParseException exception) throws SAXException {
      throw exception;
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
      throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      throw exception;
    }
  }
}
