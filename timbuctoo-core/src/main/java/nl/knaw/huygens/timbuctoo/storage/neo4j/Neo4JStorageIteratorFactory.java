package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;

import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;

class Neo4JStorageIteratorFactory {

  public Neo4JStorageIteratorFactory() {}

  public <T extends Entity> StorageIterator<T> create(Class<T> type, Iterable<? extends T> iterable) {
    // FIXME Quick fix TIM-123
    return StorageIteratorStub.newInstance(Lists.newArrayList(iterable));
  }

  public <T extends Relation> StorageIterator<T> forRelation(Class<T> relationType, List<Relationship> relationships) throws StorageException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }
}
