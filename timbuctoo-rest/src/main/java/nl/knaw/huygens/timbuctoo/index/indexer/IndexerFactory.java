package nl.knaw.huygens.timbuctoo.index.indexer;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.messages.ActionType;

public class IndexerFactory {
  private final Repository repository;
  private final IndexManager indexManager;

  @Inject
  public IndexerFactory(Repository repository, IndexManager indexManager){
    this.repository = repository;
    this.indexManager = indexManager;
  }

  public Indexer create(ActionType actionType) {
    switch (actionType) {
      case ADD:
        return new AddIndexer(indexManager);
      case MOD:
        return new UpdateIndexer(indexManager);
      case DEL:
        return new DeleteIndexer(indexManager);
      default:
        throw new IllegalArgumentException(String.format("[%s] is not supported by the IndexFactory.", actionType));
    }
  }

}
