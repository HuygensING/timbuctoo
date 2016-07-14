package nl.knaw.huygens.timbuctoo.rdf;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Ignore;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CollectionTest {

  @Test
  public void addPropertyAddsThePropertyForTheCurrentCollectionToTheEntityVertex() {
    CollectionDescription description = mock(CollectionDescription.class);
    String collectionPropertyName = "propertyName";
    when(description.createPropertyName(anyString())).thenReturn(collectionPropertyName);
    Collection instance = new Collection("vreName", mock(Vertex.class), newGraph().wrap(), description);
    Vertex entityVertex = mock(Vertex.class);
    String propValue = "propValue";

    instance.addProperty(entityVertex, "propName", propValue);

    verify(entityVertex).property(collectionPropertyName, propValue);
  }

  @Ignore
  @Test
  public void addPropertyAddsThePropertyConfigurationOfThePropertyToTheCollectionVertex() {
    fail("Yet to be implemented");
  }

}
