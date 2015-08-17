package nl.knaw.huygens.timbuctoo.index;

public interface Indexer {
  void executeFor(IndexRequest request) throws IndexException;
}
