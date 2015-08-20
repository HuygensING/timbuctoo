package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import java.time.LocalDateTime;

public interface IndexRequest {
  void setDesc(String desc);

  String getDesc();

  String toClientRep();

  Class<? extends DomainEntity> getType();

  void setType(Class<? extends DomainEntity> type);

  Status getStatus();

  void inProgress();

  void done();

  LocalDateTime getLastChanged();

  void setId(String id);

  String getId();

  StorageIterator<? extends DomainEntity> getEntities(Repository repository);

  void index(Indexer indexer);

  public enum Status {
    REQUESTED,
    IN_PROGRESS,
    DONE
  }
}
