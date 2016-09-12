package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Sets;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.Set;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PropertyHelperTest {

  @Test
  public void setCollectionPropertiesDuplicatesTheExistingPropertiesToTheNewCollection() {
    final String vreName = "vreName";
    final String entityTypeName = "newCollection";
    final Collection newCollection = mock(Collection.class);
    final CollectionDescription newCollectionDescription =
      CollectionDescription.createCollectionDescription(entityTypeName, vreName);
    when(newCollection.getDescription()).thenReturn(newCollectionDescription);
    when(newCollection.getVreName()).thenReturn("vreName");
    final CollectionDescription existingCollectionDescription1 =
      CollectionDescription.createCollectionDescription("existingCollection1", vreName);
    final Collection existingCollection1 = mock(Collection.class);
    when(existingCollection1.getDescription()).thenReturn(existingCollectionDescription1);
    final CollectionDescription existingCollectionDescription2 =
      CollectionDescription.createCollectionDescription("existingCollection2", vreName);
    final Collection existingCollection2 = mock(Collection.class);
    when(existingCollection2.getDescription()).thenReturn(existingCollectionDescription2);
    final Set<Collection> existingCollections =
      Sets.newHashSet(newCollection, existingCollection1, existingCollection2);
    final Vertex vertex = newGraph().build().addVertex();
    vertex.property(existingCollectionDescription1.createPropertyName("existing_1"), "value1");
    vertex.property(existingCollectionDescription1.createPropertyName("existing_2"), "value2");
    vertex.property(existingCollectionDescription2.createPropertyName("existing_1"), "value1");
    vertex.property(existingCollectionDescription2.createPropertyName("existing_2"), "value2");

    new PropertyHelper().setPropertiesForNewCollection(vertex, newCollection, existingCollections);

    verify(newCollection, atLeastOnce()).addProperty(vertex, "existing_1", "value1");
    verify(newCollection, atLeastOnce()).addProperty(vertex, "existing_2", "value2");
  }

  @Test
  public void setCollectionPropertiesDuplicatesTheExistingPropertiesFromTheDefaultCollection() {
    final String vreName = "vreName";
    final String entityTypeName = "newCollection";
    final Collection newCollection = mock(Collection.class);
    final CollectionDescription newCollectionDescription =
      CollectionDescription.createCollectionDescription(entityTypeName, vreName);
    when(newCollection.getDescription()).thenReturn(newCollectionDescription);
    when(newCollection.getVreName()).thenReturn("vreName");
    final Set<Collection> existingCollections = Sets.newHashSet(newCollection);
    final Vertex vertex = newGraph().build().addVertex();
    vertex.property(CollectionDescription.getDefault(vreName).createPropertyName("prop1"), "value1");
    vertex.property(CollectionDescription.getDefault(vreName).createPropertyName("prop2"), "value2");
    PropertyHelper propertyHelper = new PropertyHelper();

    propertyHelper.setPropertiesForNewCollection(vertex, newCollection, existingCollections);

    verify(newCollection).addProperty(vertex, "prop1", "value1");
    verify(newCollection).addProperty(vertex, "prop2", "value2");
  }



  @Test
  public void removeRemovesAllThePropertiesStartingWithThePrefixOfTheCollectionDescription() {
    final String vreName = "vreName";
    final CollectionDescription descriptionToRemove =
      CollectionDescription.createCollectionDescription("collection", vreName);
    final CollectionDescription otherDescription =
      CollectionDescription.createCollectionDescription("otherEntity", vreName);
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
