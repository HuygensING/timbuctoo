package nl.knaw.huygens.repository.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringWriter;

import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class HTMLGeneratorTest {

  private HTMLGenerator gen;
  private ObjectMapper mapper;
  private StringWriter writer;

  @Before
  public void setUp() throws Exception {
    mapper = new ObjectMapper();
    writer = new StringWriter();
    JsonFactory factory = new JsonFactory();
    JsonGenerator realGen = factory.createGenerator(writer);
    gen = new HTMLGenerator(realGen);
  }

  @Test
  public void testSimple() {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.setId("TCD0000000001");
    doc.name = "name";
    doc.setVariations(Lists.newArrayList("TestConcreteDoc"));

    try {
      mapper.writeValue(gen, doc);
    } catch (Exception e) {
      e.printStackTrace();
      fail("writeValue threw an exception");
    }
    String writtenHTML = writer.getBuffer().toString();
    assertEquals("<table>\n" + "<tr><th>Class</th><td>nl.knaw.huygens.repository.variation.model.TestConcreteDoc</td></tr>\n" + "<tr><th>Name</th><td>name</td></tr>\n"
        + "<tr><th>Id</th><td>TCD0000000001</td></tr>\n" + "<tr><th>Rev</th><td>0</td></tr>\n" + "<tr><th>Last Change</th><td>none</td></tr>\n" + "<tr><th>Creation</th><td>none</td></tr>\n"
        + "<tr><th>Pid</th><td>none</td></tr>\n" + "<tr><th>Variations</th><td>TestConcreteDoc;<br>\n</td></tr>\n" + "<tr><th>Current Variation</th><td>none</td></tr>\n"
        + "<tr><th>Deleted</th><td>no</td></tr>\n" + "</table>\n", writtenHTML);
  }

}
