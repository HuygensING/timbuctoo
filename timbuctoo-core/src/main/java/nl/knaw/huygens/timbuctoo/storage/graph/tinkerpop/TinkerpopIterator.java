package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.util.Iterator;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;

public class TinkerpopIterator<T extends Entity> implements StorageIterator<T> {
  private static final Logger LOG = LoggerFactory.getLogger(TinkerpopIterator.class);
  private Iterator<Vertex> delegate;
  private VertexConverter<T> converter;

  public TinkerpopIterator(VertexConverter<T> converter, Iterator<Vertex> delegate) {
    this.converter = converter;
    this.delegate = delegate;
  }

  @Override
  public boolean hasNext() {
    return delegate.hasNext();
  }

  @Override
  public T next() {
    T item = null;
    Vertex element = delegate.next();
    try {
      item = converter.convertToEntity(element);
    } catch (ConversionException e) {
      LOG.error("Element with \"{}\" cannot be converted.", ElementHelper.getIdProperty(element));
      e.printStackTrace();
    }
    return item;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Method not supported");
  }

  @Override
  public StorageIterator<T> skip(int count) {
    for (; count > 0 && delegate.hasNext(); count--) {
      delegate.next();
    }
    return this;
  }

  @Override
  public List<T> getSome(int limit) {
    List<T> some = Lists.newArrayList();
    for (; limit > 0 && hasNext(); limit--) {
      some.add(next());
    }

    return some;
  }

  @Override
  public List<T> getAll() {
    List<T> all = Lists.newArrayList();
    for (; hasNext();) {
      all.add(next());
    }

    return all;
  }

  @Override
  public void close() {}

}
