package nl.knaw.huygens.timbuctoo.index.indexer;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class DeleteIndexer extends AbstractIndexer {

  public DeleteIndexer(Repository repository, IndexManager indexManager) {
    super(repository, indexManager);
  }

  @Override
  protected void executeIndexAction(Class<? extends DomainEntity> type, String id) throws IndexException {
    indexManager.deleteEntity(type, id);
  }
}
