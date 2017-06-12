package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.SerializationTest;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

/**
 * Created on 2017-06-12 14:42.
 */
public class GexfSerializationTest extends SerializationTest {

  @Test
  public void performSerialization() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    SerializableObject graph = createGraph_01(createTypeNameStore());

    GexfSerialization gs = new GexfSerialization(out);
    graph.performSerialization(gs);
    String result = out.toString();

    //System.out.println(result);
    saveAs(out, "gephi_02.gexf");

    validate(GexfSerialization.GEXF_SCHEMA_LOCATION, result);

  }

}
