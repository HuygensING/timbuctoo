package nl.knaw.huygens.repository.providers;

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
    u.lastName = "Doe";
    u.lastName = "Flups";
    u.groups = Lists.newArrayList("A", "B", "C");
    try {
      mapper.writeValue(gen, u);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
    System.out.println(writer.getBuffer());
  }

}
