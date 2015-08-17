package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

class AddIndexer implements Indexer {

  private final Repository repository;
  private final IndexManager indexManager;

  public AddIndexer(Repository repository, IndexManager indexManager) {
    this.repository = repository;
    this.indexManager = indexManager;
  }

  @Override
  public void executeFor(IndexRequest request) throws IndexException {
    Class<? extends DomainEntity> type = request.getType();
    request.inProgress();
    for (StorageIterator<? extends DomainEntity> domainEntities = repository.getDomainEntities(type); domainEntities.hasNext(); ) {
      indexManager.addEntity(type, domainEntities.next().getId());
    }
    request.done();
  }
}
