package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;

public class Neo4JStorageIteratorFactory {

  public <T extends Entity> StorageIterator<T> create(Class<T> type, ResourceIterable<Node> searchResult) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
