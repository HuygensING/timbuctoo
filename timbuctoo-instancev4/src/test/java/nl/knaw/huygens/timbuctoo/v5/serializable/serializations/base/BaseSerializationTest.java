package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static nl.knaw.huygens.hamcrest.ListContainsItemsInAnyOrderMatcher.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created on 2017-06-08 11:52.
 */
public class BaseSerializationTest extends SerializationTest {

  private static final boolean print = false;

  @Test
  public void performSerialization() throws Exception {

    BaseSerialization bs = new BaseSerialization() {

      @Override
      public void onEdge(Edge edge) throws IOException {
        if (print) {
          System.out.println(edge);
        }
      }

      @Override
      public void onEntity(Entity entity) throws IOException {
        if (print) {
          System.out.println(entity);
        }
      }
    };
    SerializableObject graph = createGraph_01(createTypeNameStore());
    graph.performSerialization(bs);

    List<String> leafFields = Arrays.asList("foo", "name", "uri", "wroteBook");
    assertThat(bs.getLeafFieldNames(), containsInAnyOrder(leafFields));
    assertThat(bs.getEdgeCount(), equalTo(32));
    assertThat(bs.getEntityCount(), equalTo(7));
    assertThat(bs.isEntityQueueEmpty(), equalTo(true));
    assertThat(bs.isEdgeQueueEmpty(), equalTo(true));
  }
}
