package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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

  @Test
  public void removeFromCollectionRemovesTheVertexOfTheEntityFromTheCollection() {
    Vertex vertex = mock(Vertex.class);
    Collection collectionToRemoveFrom = mock(Collection.class);
    Collection otherCollection = mock(Collection.class);
    ArrayList<Collection> collections = Lists.newArrayList(collectionToRemoveFrom, otherCollection);
    Entity entity = new Entity(vertex, collections);

    entity.removeFromCollection(collectionToRemoveFrom);

    verify(collectionToRemoveFrom).remove(vertex);
    assertThat(collections, contains(otherCollection));
  }
}
