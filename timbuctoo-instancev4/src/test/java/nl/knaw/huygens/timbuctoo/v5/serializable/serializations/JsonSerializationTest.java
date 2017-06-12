package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.SerializationTest;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

/**
 *
 */
public class JsonSerializationTest extends SerializationTest {

  @Test
  public void jsonSerialization() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    SerializableObject graph = createGraph_01(createTypeNameStore());
    JsonSerialization js = new JsonSerialization(out);

    graph.performSerialization(js);
    String result = out.toString();
    System.out.println(result);

  }
}
