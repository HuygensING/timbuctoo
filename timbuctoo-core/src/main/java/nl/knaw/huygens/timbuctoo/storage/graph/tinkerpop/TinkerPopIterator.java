package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Element;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class TinkerPopIterator<T extends Entity, U extends Element> implements StorageIterator<T> {
  private static final Logger LOG = LoggerFactory.getLogger(TinkerPopIterator.class);
  private final Iterator<U> delegate;
  private final ElementConverter<T, U> converter;

  public TinkerPopIterator(ElementConverter<T, U> converter, Iterator<U> delegate) {
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
    U element = delegate.next();
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
    Stopwatch stopwatch = Stopwatch.createStarted();
    LOG.info("Skip started");
    for (; count > 0 && delegate.hasNext(); count--) {
      delegate.next();
    }
    LOG.info("Skip ended in [{}]", stopwatch.stop());
    return this;
  }

  @Override
  public List<T> getSome(int limit) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    LOG.info("Get some started");
    List<T> some = Lists.newArrayList();
    for (; limit > 0 && hasNext(); limit--) {
      some.add(next());
    }
    LOG.info("Get some ended in [{}]", stopwatch.stop());

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
