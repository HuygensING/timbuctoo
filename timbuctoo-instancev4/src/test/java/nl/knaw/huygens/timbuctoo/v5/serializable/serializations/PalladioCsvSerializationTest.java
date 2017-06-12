package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.SerializationTest;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class PalladioCsvSerializationTest extends SerializationTest {

  @Test
  public void performSerialization() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PalladioCsvSerialization cs = new PalladioCsvSerialization(out);
    SerializableObject graph = createGraph_01(createTypeNameStore());

    graph.performSerialization(cs);
    String result = out.toString();
    //System.out.println(result);
    saveAs(out, "paladio_02.csv");

    assertThat(result,
      equalTo("s_id,s_foo,s_name,s_uri,s_items,t_id,t_foo,t_name,t_uri,t_items,relation\r\n" +
        "n4,foo3,name3,uri3,,n5,foo4,name4,uri4,,fooBar\r\n" +
        "n3,foo2,name2,uri2,,n4,foo3,name3,uri3,,hasSibling\r\n" +
        "n3,foo2,name2,uri2,,n6,foo101,name101,uri101,J'ai un rêve,wroteBook\r\n" +
        "n2,foo102,name102,uri102,uri3,n7,foo3,name3,uri3,,items\r\n" +
        "n2,foo102,name102,uri102,uri3,n3,foo2,name2,uri2,,items\r\n" +
        "n1,foo1,name1,uri1,,n2,foo102,name102,uri102,uri3,hasChild\r\n" +
        "n0,foo0,name0,uri0,,n1,foo1,name1,uri1,,hasBeer\r\n"));
  }
}
