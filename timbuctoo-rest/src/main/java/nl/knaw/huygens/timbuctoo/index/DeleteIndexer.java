package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

public class DeleteIndexer implements Indexer {
  private final Repository repository;
  private final IndexManager indexManager;

  public DeleteIndexer(Repository repository, IndexManager indexManager) {
    this.repository = repository;
    this.indexManager = indexManager;
  }

  @Override
  public void executeFor(IndexRequest request) throws IndexException {
    Class<? extends DomainEntity> type = request.getType();
    request.inProgress();
    for (StorageIterator<? extends DomainEntity> domainEntities = repository.getDomainEntities(type); domainEntities.hasNext(); ) {
      String id = domainEntities.next().getId();
      indexManager.deleteEntity(type, id);
    }
    request.done();
  }
}
