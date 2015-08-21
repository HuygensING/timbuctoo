package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import java.time.LocalDateTime;

public class EntityIndexRequest implements IndexRequest {
  private final Class<? extends DomainEntity> type;
  private final String id;

  public EntityIndexRequest(Class<? extends DomainEntity> type, String id) {
    this.type = type;
    this.id = id;
  }

  @Override
  public void setDesc(String desc) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public String getDesc() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public String toClientRep() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Class<? extends DomainEntity> getType() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void setType(Class<? extends DomainEntity> type) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Status getStatus() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void inProgress() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void done() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public LocalDateTime getLastChanged() {
    throw new UnsupportedOperationException("Not implemented yet");
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
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
