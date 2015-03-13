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
  private String id;
  private final List<Node> nodes;

  public static SearchResultBuilder anEmptySearchResult() {
    return new SearchResultBuilder();
  }

  public static SearchResultBuilder aSearchResult() {
    return new SearchResultBuilder();
  }

  private SearchResultBuilder() {
    nodes = Lists.newArrayList();
  }

  public SearchResultBuilder andId(String id) {
    this.id = id;
    return this;
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
    ResourceIterator<Node> nodeIterator = IteratorUtil.asResourceIterator(nodes.iterator());

    Iterable<Node> nodesIterable = IteratorUtil.asIterable(nodeIterator);
    ResourceIterable<Node> foundNodes = Iterables.asResourceIterable(nodesIterable);
    when(db.findNodesByLabelAndProperty(label, ID_PROPERTY_NAME, id)).thenReturn(foundNodes);
  }
}
