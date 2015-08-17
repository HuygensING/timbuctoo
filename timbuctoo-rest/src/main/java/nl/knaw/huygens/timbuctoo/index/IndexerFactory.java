package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.ActionType;

public class IndexerFactory {
  private final Repository repository;
  private final IndexManager indexManager;

  public IndexerFactory(Repository repository, IndexManager indexManager){
    this.repository = repository;
    this.indexManager = indexManager;
  }

  public Indexer create(ActionType actionType) {
    switch (actionType) {
      case ADD:
        return new AddIndexer(repository, indexManager);
      case MOD:
        return new UpdateIndexer(repository, indexManager);
      case DEL:
        return new DeleteIndexer();
      default:
        throw new IllegalArgumentException(String.format("[%s] is not supported by the IndexFactory.", actionType));
    }
  }

}
