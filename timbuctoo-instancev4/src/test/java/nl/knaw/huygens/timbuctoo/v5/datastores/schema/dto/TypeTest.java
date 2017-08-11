package nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.util.jacksonserializers.TimbuctooCustomSerializers;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TypeTest {

  @Test
  public void isSerializable() throws IOException {
    Type type = new Type("http://example.org/myType");
    type.getOrCreatePredicate("http://example.org/myPredicate", Direction.OUT);
    type.getOrCreatePredicate("http://example.org/myPredicate", Direction.IN);

    ObjectMapper mapper = new ObjectMapper()
      .registerModule(new Jdk8Module())
      .registerModule(new GuavaModule())
      .registerModule(new TimbuctooCustomSerializers())
      .enable(SerializationFeature.INDENT_OUTPUT);
    String result = mapper.writeValueAsString(type);

    Type loadedType = mapper.readValue(result, Type.class);

    assertThat(loadedType, is(type));
  }
}
