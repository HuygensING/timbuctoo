package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;

class Neo4JStorageIteratorFactory {

  private PropertyContainerConverterFactory propertyContainerConverterFactory;

  public Neo4JStorageIteratorFactory(PropertyContainerConverterFactory propertyContainerConverterFactory) {
    this.propertyContainerConverterFactory = propertyContainerConverterFactory;
  }

  public <T extends Entity> StorageIterator<T> create(Class<T> type, ResourceIterator<Node> searchResult) {
    NodeConverter<T> nodeConverter = propertyContainerConverterFactory.createForType(type);
    return new Neo4JStorageIterator<T>(searchResult, nodeConverter);
  }

  static class Neo4JStorageIterator<T extends Entity> implements StorageIterator<T> {

    final ResourceIterator<Node> delegate;
    final NodeConverter<T> converter;

    public Neo4JStorageIterator(ResourceIterator<Node> delegate, NodeConverter<T> converter) {
      this.delegate = delegate;
      this.converter = converter;
    }

    @Override
    public boolean hasNext() {
      throw new UnsupportedOperationException("Yet to be implemented");
    }

    @Override
    public T next() {
      throw new UnsupportedOperationException("Yet to be implemented");
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Yet to be implemented");
    }

    @Override
    public int size() {
      throw new UnsupportedOperationException("Yet to be implemented");
    }

    @Override
    public StorageIterator<T> skip(int count) {
      throw new UnsupportedOperationException("Yet to be implemented");
    }

    @Override
    public List<T> getSome(int limit) {
      throw new UnsupportedOperationException("Yet to be implemented");
    }

    @Override
    public List<T> getAll() {
      throw new UnsupportedOperationException("Yet to be implemented");
    }

    @Override
    public void close() {
      throw new UnsupportedOperationException("Yet to be implemented");
    }

  }
}
