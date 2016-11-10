package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

public class PropertyHelperTest {

  @Test
  public void movePropertiesMovesThePropertiesToTheNewCollection() {
    String vreName = "vreName";

    TinkerpopGraphManager graphManager = newGraph().wrap();
    Vertex oldColVertex = graphManager.getGraph().addVertex(
      ENTITY_TYPE_NAME_PROPERTY_NAME, "oldCollection"
    );
    Collection oldCollection = new Collection(vreName, oldColVertex, graphManager);

    Vertex newColVertex = graphManager.getGraph().addVertex(
      ENTITY_TYPE_NAME_PROPERTY_NAME, "newCollection"
    );
    Collection newCollection = new Collection(vreName, newColVertex, graphManager);

    Vertex vertex = graphManager.getGraph().addVertex(
      "vreNameoldCollection_prop", "value1",
      "vreNameoldCollection2_prop", "value1"
    );

    Entity entity = new Entity(vertex, Sets.newHashSet(oldCollection, newCollection));

    new PropertyHelper().movePropertiesToNewCollection(entity, oldCollection, newCollection);

    assertThat(vertex.value("vreNameoldCollection2_prop"), is("value1"));
    assertThat(vertex.value("vreNamenewCollection_prop"), is("value1"));
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
