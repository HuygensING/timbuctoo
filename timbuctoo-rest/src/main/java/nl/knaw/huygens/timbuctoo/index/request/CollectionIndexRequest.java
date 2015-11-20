package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

class CollectionIndexRequest extends AbstractIndexRequest {
  private final Repository repository;

  public CollectionIndexRequest(IndexerFactory indexerFactory, ActionType actionType, Class<? extends DomainEntity> type, Repository repository) {
    super(indexerFactory, actionType, type);
    this.repository = repository;
  }

  @Override
  public void execute() throws IndexException {
    Indexer indexer = getIndexerFactory().create(this.getActionType());
    Class<? extends DomainEntity> type = getType();

    StorageIterator<? extends DomainEntity> entities = repository.getDomainEntities(type);

    for (; entities.hasNext(); ) {
      indexer.executeIndexAction(type, entities.next().getId());
    }
  }

  @Override
  public Action toAction() {
    return new Action(getActionType(), getType());
  }
}
