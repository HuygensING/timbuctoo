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
  public T withPropertyContainer(U propertyContainer) {
    addPropertyContainer(propertyContainer);
    return (T) this;
  }

  private void addPropertyContainer(U propertyContainer) {
    propertyContainers.add(propertyContainer);
  }

  /**
   * Method for a better readable code. Does the same as withPropertyContainer.
   * @param propertyContainer the node
   * @return this
   */
  @SuppressWarnings("unchecked")
  public T andPropertyContainer(U propertyContainer) {
    addPropertyContainer(propertyContainer);
    return (T) this;
  }

  public ResourceIterable<U> asIterable() {
    ResourceIterator<U> nodeIterator = asIterator();

    Iterable<U> nodesIterable = IteratorUtil.asIterable(nodeIterator);
    ResourceIterable<U> foundNodes = Iterables.asResourceIterable(nodesIterable);

    return foundNodes;
  }

  public ResourceIterator<U> asIterator() {
    return IteratorUtil.asResourceIterator(propertyContainers.iterator());
  }

}