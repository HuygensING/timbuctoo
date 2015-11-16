package nl.knaw.huygens.timbuctoo.index.indexer;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequest;
import nl.knaw.huygens.timbuctoo.messages.ActionType;

public class IndexerFactory {
  private final IndexManager indexManager;

  @Inject
  public IndexerFactory(IndexManager indexManager){
    this.indexManager = indexManager;
  }

  public Indexer create(IndexRequest indexRequest) {
    ActionType actionType = indexRequest.getActionType();
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
