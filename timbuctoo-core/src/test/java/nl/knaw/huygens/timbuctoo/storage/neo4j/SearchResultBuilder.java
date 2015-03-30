package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.IteratorUtil;

import com.google.common.collect.Lists;

public class SearchResultBuilder<U extends PropertyContainer, T extends SearchResultBuilder<U, T>> {

  protected final List<U> propertyContainers;

  protected SearchResultBuilder() {
    propertyContainers = Lists.newArrayList();
  }

  @SuppressWarnings("unchecked")
  public T withNode(U node) {
    addNode(node);
    return (T) this;
  }

  private void addNode(U node) {
    propertyContainers.add(node);
  }

  /**
   * Method for a better readable code. Does the same as withNode.
   * @param node the node
   * @return this
   */
  @SuppressWarnings("unchecked")
  public T andNode(U node) {
    addNode(node);
    return (T) this;
  }

  public ResourceIterable<U> build() {
    ResourceIterator<U> nodeIterator = IteratorUtil.asResourceIterator(propertyContainers.iterator());

    Iterable<U> nodesIterable = IteratorUtil.asIterable(nodeIterator);
    ResourceIterable<U> foundNodes = Iterables.asResourceIterable(nodesIterable);

    return foundNodes;
  }

}