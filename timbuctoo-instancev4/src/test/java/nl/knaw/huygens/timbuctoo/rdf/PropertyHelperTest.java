package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class PropertyHelperTest {

  @Test
  public void setCollectionPropertiesDuplicatesTheExistingPropertiesToTheNewCollection() {
    final String vreName = "vreName";
    final String entityTypeName = "newCollection";
    final CollectionDescription newCollectionDescription = new CollectionDescription(entityTypeName, vreName);
    final CollectionDescription existingCollectionDescription1 =
      new CollectionDescription("existingCollection1", vreName);
    final CollectionDescription existingCollectionDescription2 =
      new CollectionDescription("existingCollection2", vreName);
    final List<CollectionDescription> existingCollections = Lists.newArrayList(
      existingCollectionDescription1,
      existingCollectionDescription2,
      newCollectionDescription
    );
    final Vertex vertex = newGraph().build().addVertex();
    vertex.property(existingCollectionDescription1.createPropertyName("existing_1"), "value1");
    vertex.property(existingCollectionDescription1.createPropertyName("existing_2"), "value2");
    vertex.property(existingCollectionDescription2.createPropertyName("existing_1"), "value1");
    vertex.property(existingCollectionDescription2.createPropertyName("existing_2"), "value2");

    new PropertyHelper().setPropertiesForNewCollection(vertex, newCollectionDescription, existingCollections);

    assertThat(vertex, likeVertex()
      .withProperty(newCollectionDescription.createPropertyName("existing_1"), "value1")
      .withProperty(newCollectionDescription.createPropertyName("existing_2"), "value2")
    );
  }

  @Test
  public void setCollectionPropertiesDuplicatesTheExistingPropertiesFromTheDefaultCollection() {
    final String vreName = "vreName";
    final String entityTypeName = "newCollection";
    final CollectionDescription newCollectionDescription = new CollectionDescription(entityTypeName, vreName);
    final List<CollectionDescription> existingCollections = Lists.newArrayList(newCollectionDescription);
    final Vertex vertex = newGraph().build().addVertex();
    vertex.property(CollectionDescription.getDefault(vreName).createPropertyName("prop1"), "value1");
    vertex.property(CollectionDescription.getDefault(vreName).createPropertyName("prop2"), "value2");

    new PropertyHelper().setPropertiesForNewCollection(vertex, newCollectionDescription, existingCollections);

    assertThat(vertex, likeVertex()
      .withProperty(newCollectionDescription.createPropertyName("prop1"), "value1")
      .withProperty(newCollectionDescription.createPropertyName("prop2"), "value2")
    );
  }

  @Test
  public void removeRemovesAllThePropertiesStartingWithThePrefixOfTheCollectionDescription() {
    final String vreName = "vreName";
    final CollectionDescription descriptionToRemove = new CollectionDescription("collection", vreName);
    final CollectionDescription otherDescription = new CollectionDescription("otherEntity", vreName);
    final Vertex vertex = newGraph().build().addVertex();
    vertex.property(descriptionToRemove.createPropertyName("prop1"), "value1");
    vertex.property(descriptionToRemove.createPropertyName("prop2"), "value2");
    String otherCollectionProp1 = otherDescription.createPropertyName("prop1");
    vertex.property(otherCollectionProp1, "value2");
    String otherCollectionProp2 = otherDescription.createPropertyName("prop2");
    vertex.property(otherCollectionProp2, "value2");
    PropertyHelper instance = new PropertyHelper();

    instance.removeProperties(vertex, descriptionToRemove);

    assertThat(vertex.keys(), containsInAnyOrder(otherCollectionProp1, otherCollectionProp2));
  }
}
