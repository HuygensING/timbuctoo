package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

class RelationIndexRequest extends AbstractIndexRequest{
  private final String id;

  protected RelationIndexRequest(IndexerFactory indexerFactory, ActionType actionType, Class<? extends DomainEntity> type, String id) {
    super(indexerFactory, actionType, type);
    this.id = id;
  }

  @Override
  public void execute() throws IndexException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Action toAction() {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
