package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

class MongoStorageIterator<T extends Entity> implements StorageIterator<T> {

  private final Class<T> type;
  private final DBCursor delegate;
  private final EntityReducer reducer;
  private boolean closed;

  public MongoStorageIterator(Class<T> type, DBCursor delegate, EntityReducer reducer) {
    this.type = type;
    this.delegate = Preconditions.checkNotNull(delegate);
    this.reducer = reducer;
    this.closed = false;
  }

  @Override
  public T next() {
    try {
      DBObject next = delegate.next();
      return reducer.reduceVariation(type, toJsonNode(next));
    } catch (NoSuchElementException e) {
      close();
      throw e;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public void skip(int count) {
    delegate.skip(count);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T> getSome(int limit) {
    List<T> list = Lists.newArrayList();
    while (limit-- > 0 && delegate.hasNext()) {
      DBObject next;
      try {
        next = delegate.next();
      } catch (Exception ex) {
        // Gotta love how mongo documents that there's a nomoreelements exception... nuh-uh.
        break;
      }

      try {
        list.add((T) reducer.reduceVariation((Class<? extends DomainEntity>) type, toJsonNode(next)));
      } catch (IOException e) {
        e.printStackTrace();
        list.add(null);
      }
    }
    close();
    return list;
  }

  @Override
  public boolean hasNext() {
    boolean rv = delegate.hasNext();
    if (!rv) {
      close();
    }
    return rv;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    synchronized (this) {
      if (!closed) {
        closed = true;
        delegate.close();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private JsonNode toJsonNode(DBObject object) throws IOException {
    if (object instanceof JacksonDBObject) {
      return (((JacksonDBObject<JsonNode>) object).getObject());
    } else if (object instanceof DBJsonNode) {
      return ((DBJsonNode) object).getDelegate();
    } else {
      throw new IOException("Unknown DBObject type");
    }
  }

}
