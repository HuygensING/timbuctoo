package nl.knaw.huygens.timbuctoo.index.indexer;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

public abstract class AbstractIndexer implements Indexer {
  protected final Repository repository;
  protected final IndexManager indexManager;

  public AbstractIndexer(Repository repository, IndexManager indexManager) {
    this.indexManager = indexManager;
    this.repository = repository;
  }

  @Override
  public final void executeFor(IndexRequest request) throws IndexException {
    Class<? extends DomainEntity> type = request.getType();
    request.inProgress();
    for (StorageIterator<? extends DomainEntity> domainEntities = request.getEntities(repository); domainEntities.hasNext(); ) {
      String id = domainEntities.next().getId();
      executeIndexAction(type, id);
    }
    request.done();
  }

  protected abstract void executeIndexAction(Class<? extends DomainEntity> type, String id) throws IndexException;

  protected IndexManager getIndexManager() {
    return indexManager;
  }
}
