package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_DB_PROPERTY_NAME;
import static org.mockito.Mockito.when;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

public class NodeSearchResultBuilder extends SearchResultBuilder<Node, NodeSearchResultBuilder> {
  private Label label;
  private String value;
  private String propertyName;

  public static NodeSearchResultBuilder anEmptyNodeSearchResult() {
    return new NodeSearchResultBuilder();
  }

  public static NodeSearchResultBuilder aNodeSearchResult() {
    return new NodeSearchResultBuilder();
  }

  private NodeSearchResultBuilder() {
    super();
  }

  public NodeSearchResultBuilder andPropertyWithValue(String propertyName, String value) {
    this.propertyName = propertyName;
    this.value = value;
    return this;
  }

  public NodeSearchResultBuilder andId(String id) {
    return andPropertyWithValue(ID_DB_PROPERTY_NAME, id);
  }

  public NodeSearchResultBuilder forLabel(Label label) {
    this.label = label;
    return this;
  }

  public void foundInDB(GraphDatabaseService db) {
    when(db.findNodesByLabelAndProperty(label, propertyName, value)).thenReturn(asIterable());
  }

}
