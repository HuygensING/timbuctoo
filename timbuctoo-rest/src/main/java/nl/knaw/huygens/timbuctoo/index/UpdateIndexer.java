package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

class UpdateIndexer implements Indexer{
  private final Repository repository;
  private final IndexManager indexManager;

  public UpdateIndexer(Repository repository, IndexManager indexManager) {
    this.repository = repository;
    this.indexManager = indexManager;
  }

  @Override
  public void executeFor(IndexRequest request) throws IndexException {
    Class<? extends DomainEntity> type = request.getType();
    request.inProgress();
    for (StorageIterator<? extends DomainEntity> domainEntities = repository.getDomainEntities(type); domainEntities.hasNext(); ) {
      String id = domainEntities.next().getId();
      indexManager.updateEntity(type, id);
    }
    request.done();
  }
}
