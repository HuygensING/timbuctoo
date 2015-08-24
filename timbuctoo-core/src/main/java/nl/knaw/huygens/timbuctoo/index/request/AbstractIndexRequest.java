package nl.knaw.huygens.timbuctoo.index.request;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractIndexRequest implements IndexRequest {

  private final Class<? extends DomainEntity> type;
  private IndexRequestStatus indexRequestStatus;
  Status status;
  private LocalDateTime lastChanged;

  protected AbstractIndexRequest(Class<? extends DomainEntity> type, IndexRequestStatus indexRequestStatus) {
    this.type = type;
    this.indexRequestStatus = indexRequestStatus;
    lastChanged = LocalDateTime.now();
  }

  @Override
  public Map<String, Object> toClientRep() {
    HashMap<String, Object> clientRepresentation = Maps.newHashMap();
    clientRepresentation.put("desc", getDesc());
    clientRepresentation.put("status", getStatus());
    return clientRepresentation;
  }

  protected abstract String getDesc();

  @Override
  public Class<? extends DomainEntity> getType() {
    return type;
  }

  @Override
  public Status getStatus() {
    return indexRequestStatus.getStatus();
  }

  @Override
  public void inProgress() {
    this.status = Status.IN_PROGRESS;
    lastChanged = LocalDateTime.now();
  }

  @Override
  public void done() {
    this.status = Status.DONE;
    lastChanged = LocalDateTime.now();
  }

  @Override
  public LocalDateTime getLastChanged() {
    return lastChanged;
  }

  @Override
  public boolean canBeDiscarded(int timeout) {
    if (getStatus() == Status.DONE && isTimedOut(timeout)) {
      return true;
    }
    return false;
  }

  protected boolean isTimedOut(int timeout) {
    return LocalDateTime.now().minus(timeout, ChronoUnit.MILLIS).isAfter(lastChanged);
  }

  @Override
  public final void execute(Indexer indexer) throws IndexException {
    indexRequestStatus = indexRequestStatus.inProgress();
    executeIndexAction(indexer);
    indexRequestStatus = indexRequestStatus.done();
  }

  protected abstract void executeIndexAction(Indexer indexer) throws IndexException;
}
