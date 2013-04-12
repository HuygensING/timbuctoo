package nl.knaw.huygens.repository.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringWriter;

import nl.knaw.huygens.repository.model.User;

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
    User u = new User();
    u.firstName = "Doe";
    u.lastName = "Flups";
    u.groups = Lists.newArrayList("A", "B", "C");
    u.setId("USR0000000001");

    u.setVariations(Lists.newArrayList("User"));

    try {
      mapper.writeValue(gen, u);
    } catch (Exception e) {
      e.printStackTrace();
      fail("writeValue threw an exception");
    }
    String writtenHTML = writer.getBuffer().toString();
    assertEquals("<table>\n" + "<tr><th>Pw Hash</th><td>none</td></tr>\n" + "<tr><th>Email</th><td>none</td></tr>\n" + "<tr><th>First Name</th><td>Doe</td></tr>\n"
        + "<tr><th>Last Name</th><td>Flups</td></tr>\n" + "<tr><th>Groups</th><td>A;<br>\n" + "B;<br>\n" + "C;<br>\n" + "</td></tr>\n" + "<tr><th>Id</th><td>USR0000000001</td></tr>\n"
        + "<tr><th>Rev</th><td>0</td></tr>\n" + "<tr><th>Last Change</th><td>none</td></tr>\n" + "<tr><th>Creation</th><td>none</td></tr>\n" + "<tr><th>Pid</th><td>none</td></tr>\n"
        + "<tr><th>Variations</th><td>User;<br>\n</td></tr>\n" + "<tr><th>Current Variation</th><td>none</td></tr>\n" + "<tr><th>Deleted</th><td>no</td></tr>\n" + "</table>\n", writtenHTML);
  }

}
