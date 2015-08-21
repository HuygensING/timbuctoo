package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import java.time.LocalDateTime;

class EntityIndexRequest extends AbstractIndexRequest {
  private Status status;
  private LocalDateTime lastChanged;
  private final Class<? extends DomainEntity> type;
  private final String id;

  public EntityIndexRequest(Class<? extends DomainEntity> type, String id) {
    lastChanged = LocalDateTime.now();
    status = Status.REQUESTED;
    this.type = type;
    this.id = id;
  }

  @Override
  protected String getDesc() {
    return String.format("Index request for [%s] with id [%s]", TypeNames.getExternalName(type), id);
  }

  @Override
  public void execute(Indexer indexer) throws IndexException {
    indexer.executeIndexAction(type, id);
  }
}
