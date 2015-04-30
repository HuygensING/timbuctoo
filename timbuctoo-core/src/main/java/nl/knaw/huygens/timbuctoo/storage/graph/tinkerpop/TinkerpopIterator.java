package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.util.Iterator;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import com.tinkerpop.blueprints.Vertex;

public class TinkerpopIterator<T extends Entity> implements StorageIterator<T> {

  public TinkerpopIterator(VertexConverter<T> converter, Iterator<Vertex> iterator) {
    // TODO Auto-generated constructor stub
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
