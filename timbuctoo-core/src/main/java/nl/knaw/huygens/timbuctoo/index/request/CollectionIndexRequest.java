package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import java.time.LocalDateTime;

class CollectionIndexRequest implements IndexRequest {
  private final Class<? extends DomainEntity> type;
  private final Repository repository;
  private LocalDateTime lastChanged;
  private String desc;
  private Status status;

  public CollectionIndexRequest(Class<? extends DomainEntity> type, Repository repository) {
    this.repository = repository;
    this.status = Status.REQUESTED;
    this.lastChanged = LocalDateTime.now();
    this.type = type;
  }

  @Override
  public void setDesc(String desc) {
    this.desc = desc;
  }

  @Override
  public String getDesc() {
    return desc;
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
  public void setType(Class<? extends DomainEntity> type) {
//    this.type = type;
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
  public void setId(String id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public String getId() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public StorageIterator<? extends DomainEntity> getEntities(Repository repository) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void execute(Indexer indexer) throws IndexException {
    for(StorageIterator<? extends DomainEntity> iterator = repository.getDomainEntities(type); iterator.hasNext();){
      indexer.executeIndexAction(type, iterator.next().getId());
    }
  }
}
