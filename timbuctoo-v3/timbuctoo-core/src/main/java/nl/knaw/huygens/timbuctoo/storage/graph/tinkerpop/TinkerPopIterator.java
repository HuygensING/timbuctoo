package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
    LOG.debug("Skip started");
    for (; count > 0 && delegate.hasNext(); count--) {
      delegate.next();
    }
    LOG.debug("Skip ended in [{}]", stopwatch.stop());
    return this;
  }

  @Override
  public List<T> getSome(int limit) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    LOG.debug("Get some started");
    List<T> some = Lists.newArrayList();
    for (; limit > 0 && hasNext(); limit--) {
      some.add(next());
    }
    LOG.debug("Get some ended in [{}]", stopwatch.stop());

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
