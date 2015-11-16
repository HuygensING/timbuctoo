package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

class CollectionIndexRequest extends AbstractIndexRequest {
  private final Repository repository;

  public CollectionIndexRequest(Class<? extends DomainEntity> type, Repository repository) {
    super(type);
    this.repository = repository;
  }

  @Override
  protected void executeIndexAction(Indexer indexer) throws IndexException {
    Class<? extends DomainEntity> type = getType();

    StorageIterator<? extends DomainEntity> entities = repository.getDomainEntities(type);

    for (; entities.hasNext();) {
      indexer.executeIndexAction(type, entities.next().getId());
    }
  }
}
