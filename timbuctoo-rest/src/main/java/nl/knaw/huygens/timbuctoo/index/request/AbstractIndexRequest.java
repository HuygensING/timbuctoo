package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

abstract class AbstractIndexRequest implements IndexRequest {

  private final Class<? extends DomainEntity> type;

  protected AbstractIndexRequest(Class<? extends DomainEntity> type) {
    this.type = type;
  }

  @Override
  public Class<? extends DomainEntity> getType() {
    return type;
  }

  @Override
  public final void execute(Indexer indexer) throws IndexException {
    executeIndexAction(indexer);
  }

  protected abstract void executeIndexAction(Indexer indexer) throws IndexException;
}
