package nl.knaw.huygens.timbuctoo.index.indexer;

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

class UpdateIndexer implements Indexer{

  private final IndexManager indexManager;

  public UpdateIndexer(IndexManager indexManager) {
    this.indexManager = indexManager;
  }

  @Override
  public void executeIndexAction(Class<? extends DomainEntity> type, String id) throws IndexException {
    indexManager.updateEntity(type, id);
  }
}
