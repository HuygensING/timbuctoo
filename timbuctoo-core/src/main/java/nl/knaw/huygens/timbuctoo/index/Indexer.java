package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.index.request.IndexRequest;

public interface Indexer {
  void executeFor(IndexRequest request) throws IndexException;
}
