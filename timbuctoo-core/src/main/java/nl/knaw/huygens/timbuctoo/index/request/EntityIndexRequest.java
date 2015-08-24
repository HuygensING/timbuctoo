package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

class EntityIndexRequest extends AbstractIndexRequest {
  private final String id;

  public EntityIndexRequest(Class<? extends DomainEntity> type, String id) {
    this(type, id, IndexRequestStatus.requested());
  }

  EntityIndexRequest(Class<? extends DomainEntity> type, String id, IndexRequestStatus indexRequestStatus) {
    super(type, indexRequestStatus);
    this.id = id;
  }

  @Override
  protected String getDesc() {
    return String.format("Index request for [%s] with id [%s]", TypeNames.getExternalName(getType()), id);
  }

  @Override
  protected void executeIndexAction(Indexer indexer) throws IndexException {
    indexer.executeIndexAction(getType(), id);
  }
}
