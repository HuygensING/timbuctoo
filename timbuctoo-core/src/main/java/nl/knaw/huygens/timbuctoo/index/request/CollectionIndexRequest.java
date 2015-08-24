package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

class CollectionIndexRequest extends AbstractIndexRequest {
  private final Repository repository;

  public CollectionIndexRequest(Class<? extends DomainEntity> type, Repository repository) {
    this(type, repository, IndexRequestStatus.requested());
  }

  CollectionIndexRequest(Class<? extends DomainEntity> type, Repository repository, IndexRequestStatus indexRequestStatus) {
    super(type, indexRequestStatus);
    this.repository = repository;
  }

  @Override
  protected String getDesc() {
    return String.format("Index request for [%s]", TypeNames.getExternalName(getType()));
  }

  @Override
  protected void executeIndexAction(Indexer indexer) throws IndexException {
    Class<? extends DomainEntity> type = getType();
    for (StorageIterator<? extends DomainEntity> iterator = repository.getDomainEntities(type); iterator.hasNext(); ) {
      indexer.executeIndexAction(type, iterator.next().getId());
    }
  }

}
