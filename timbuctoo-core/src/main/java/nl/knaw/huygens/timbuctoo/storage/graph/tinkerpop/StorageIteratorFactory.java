package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.util.Iterator;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import com.tinkerpop.blueprints.Vertex;

class StorageIteratorFactory {

  public <T extends Entity> StorageIterator<T> create(Class<T> type, Iterator<Vertex> iterator) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
