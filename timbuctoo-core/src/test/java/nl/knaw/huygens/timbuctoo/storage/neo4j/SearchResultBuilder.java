package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static org.mockito.Mockito.when;

import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.IteratorUtil;

import com.google.common.collect.Lists;

public class SearchResultBuilder {
  private Label label;
  private String value;
  private final List<Node> nodes;
  private String propertyName;

  public static SearchResultBuilder anEmptySearchResult() {
    return new SearchResultBuilder();
  }

  public static SearchResultBuilder aSearchResult() {
    return new SearchResultBuilder();
  }

  private SearchResultBuilder() {
    nodes = Lists.newArrayList();
  }

  public SearchResultBuilder andPropertyWithValue(String propertyName, String value) {
    this.propertyName = propertyName;
    this.value = value;
    return this;
  }

  public SearchResultBuilder andId(String id) {
    return andPropertyWithValue(ID_PROPERTY_NAME, id);
  }

  public SearchResultBuilder forLabel(Label label) {
    this.label = label;
    return this;
  }

  public SearchResultBuilder withNode(Node node) {
    addNode(node);
    return this;
  }

  private void addNode(Node node) {
    nodes.add(node);
  }

  /**
   * Method for a better readable code. Does the same as withNode.
   * @param node the node
   * @return this
   */
  public SearchResultBuilder andNode(Node node) {
    addNode(node);
    return this;
  }

  public void foundInDB(GraphDatabaseService db) {
    when(db.findNodesByLabelAndProperty(label, propertyName, value)).thenReturn(build());
  }

  public ResourceIterable<Node> build() {
    ResourceIterator<Node> nodeIterator = IteratorUtil.asResourceIterator(nodes.iterator());

    Iterable<Node> nodesIterable = IteratorUtil.asIterable(nodeIterator);
    ResourceIterable<Node> foundNodes = Iterables.asResourceIterable(nodesIterable);

    return foundNodes;
  }

}
