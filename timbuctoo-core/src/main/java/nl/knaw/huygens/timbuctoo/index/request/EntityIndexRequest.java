package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;

import java.time.LocalDateTime;

class EntityIndexRequest implements IndexRequest {
  private String desc;
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
  public void setDesc(String desc) {
    this.desc = desc;
  }

  @Override
  public String toClientRep() {
    return String.format("{\"desc\":\"%s\", \"status\":\"%s\"}", desc, status);
  }

  @Override
  public Class<? extends DomainEntity> getType() {
    return type;
  }

  @Override
  public Status getStatus() {
    return status;
  }

  @Override
  public void inProgress() {
    status = Status.IN_PROGRESS;
    lastChanged = LocalDateTime.now();
  }

  @Override
  public void done() {
    status = Status.DONE;
    lastChanged = LocalDateTime.now();
  }

  @Override
  public LocalDateTime getLastChanged() {
    return lastChanged;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public StorageIterator<? extends DomainEntity> getEntities(Repository repository) {
    if (id != null) {
      return StorageIteratorStub.newInstance(repository.getEntityOrDefaultVariation(type, id));
    }

    return repository.getDomainEntities(type);
  }

  @Override
  public void execute(Indexer indexer) throws IndexException {
    indexer.executeIndexAction(type, id);
  }
}
