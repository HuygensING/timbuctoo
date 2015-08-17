package nl.knaw.huygens.timbuctoo.index.indexer;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

class UpdateIndexer extends AbstractIndexer {

  public UpdateIndexer(Repository repository, IndexManager indexManager) {
    super(repository, indexManager);
  }

  @Override
  protected void executeIndexAction(Class<? extends DomainEntity> type, String id) throws IndexException {
    indexManager.updateEntity(type, id);
  }
}
