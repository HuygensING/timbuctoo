package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

abstract class AbstractIndexRequest implements IndexRequest {

  private final ActionType actionType;
  private final Class<? extends DomainEntity> type;

  protected AbstractIndexRequest(ActionType actionType, Class<? extends DomainEntity> type) {
    this.actionType = actionType;
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

  @Override
  public ActionType getActionType() {
    return actionType;
  }

}
