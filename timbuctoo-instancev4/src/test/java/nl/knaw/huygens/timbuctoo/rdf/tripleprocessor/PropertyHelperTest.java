package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;

public class PropertyHelperTest {

  @Test
  public void setCollectionPropertiesDuplicatesTheExistingPropertiesToTheNewCollection() {
    final String vreName = "vreName";
    final String entityTypeName = "newCollection";
    final CollectionDescription newCollectionDescription = new CollectionDescription(entityTypeName, vreName);
    final CollectionDescription existingCollectionDescription =
      new CollectionDescription("existingCollection", vreName);
    final List<CollectionDescription> existingCollections = Lists.newArrayList(existingCollectionDescription);
    final Vertex vertex = newGraph().build().addVertex();
    vertex.property(existingCollectionDescription.createPropertyName("existing"), "value1");
    vertex.property(CollectionDescription.getDefault(vreName).createPropertyName("unknownExisting"), "value2");

    new PropertyHelper().setCollectionProperties(vertex, newCollectionDescription, existingCollections);

    assertThat(vertex, likeVertex()
      .withProperty(newCollectionDescription.createPropertyName("existing"), "value1")
      .withProperty(newCollectionDescription.createPropertyName("unknownExisting"), "value2"));
  }

  @Test
  public void setCollectionPropertiesRemovesTheUnknownCollectionProperties() {
    final String vreName = "vreName";
    final String entityTypeName = "newCollection";
    final CollectionDescription newCollectionDescription = new CollectionDescription(entityTypeName, vreName);
    final List<CollectionDescription> existingCollections = Lists.newArrayList();
    final Vertex vertex = newGraph().build().addVertex();
    final String unknownExisting = CollectionDescription.getDefault(vreName).createPropertyName("unknownExisting");

    vertex.property(unknownExisting, "value2");

    new PropertyHelper().setCollectionProperties(vertex, newCollectionDescription, existingCollections);

    assertThat(vertex, likeVertex()
      .withoutProperty(unknownExisting));
  }

}
