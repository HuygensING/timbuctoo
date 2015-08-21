package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import java.time.LocalDateTime;

abstract class AbstractIndexRequest implements IndexRequest {

  private Class<? extends DomainEntity> type;
  private Status status;
  private LocalDateTime lastChanged;

  protected AbstractIndexRequest() {
    status = Status.REQUESTED;
    lastChanged = LocalDateTime.now();
  }

  @Override
  public String toClientRep() {
    return String.format("{\"desc\":\"%s\", \"status\":\"%s\"}", getDesc(), status);
  }

  protected abstract String getDesc() ;

  @Override
  public Class<? extends DomainEntity> getType() {
    return type;
  }

  private void setType(Class<? extends DomainEntity> type) {
    this.type = type;
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

}
