package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EntityTest {
  @Test
  public void addPropertyAddsThePropertyToTheEntityForEachCollectionTheEntityIsConnectedTo() {
    Vertex vertex = mock(Vertex.class);
    Collection collection1 = mock(Collection.class);
    Collection collection2 = mock(Collection.class);
    Entity entity = new Entity(vertex, Lists.newArrayList(collection1, collection2));
    String propName = "propName";
    String value = "value";

    entity.addProperty(propName, value);

    verify(collection1).addProperty(vertex, propName, value);
    verify(collection2).addProperty(vertex, propName, value);
  }
}
