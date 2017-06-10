package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.SerializationTest;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created on 2017-06-08 13:05.
 */
public class CsvSerializationTest extends SerializationTest {

  @Test
  public void performSerialization() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CsvSerialization cs = new CsvSerialization(out);
    SerializableObject graph = createGraph_01(createTypeNameStore());

    graph.performSerialization(cs);
    String result = out.toString();
    //System.out.println(result);
    //saveAs(out, "csv_01.csv");

    assertThat(result,
      equalTo("s_id,s_wroteBook,s_foo,s_name,s_uri,t_id,t_wroteBook,t_foo,t_name,t_uri,relation\n" +
      "n3,,foo3,name3,uri3,n4,,foo4,name4,uri4,fooBar\n" +
      "n2,J'ai un rêve,foo2,name2,uri2,n3,,foo3,name3,uri3,hasSibling\n" +
      "n1,,foo1,name1,uri1,n5,,foo3,name3,uri3,hasChild\n" +
      "n1,,foo1,name1,uri1,n2,J'ai un rêve,foo2,name2,uri2,hasChild\n" +
      "n0,,foo0,name0,uri0,n1,,foo1,name1,uri1,hasBeer\n"));
  }
}
