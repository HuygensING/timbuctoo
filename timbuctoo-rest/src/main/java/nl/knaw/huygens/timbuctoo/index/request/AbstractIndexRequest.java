package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

abstract class AbstractIndexRequest implements IndexRequest {

  private final IndexerFactory indexerFactory;
  private final ActionType actionType;
  private final Class<? extends DomainEntity> type;

  protected AbstractIndexRequest(IndexerFactory indexerFactory, ActionType actionType, Class<? extends DomainEntity> type) {
    this.indexerFactory = indexerFactory;
    this.actionType = actionType;
    this.type = type;
  }

  @Override
  public Class<? extends DomainEntity> getType() {
    return type;
  }

  @Override
  public abstract void execute() throws IndexException;

  protected IndexerFactory getIndexerFactory(){
    return this.indexerFactory;
  }

  @Override
  public ActionType getActionType() {
    return actionType;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
