package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import java.time.LocalDateTime;

public interface IndexRequest {
  String toClientRep();

  Class<? extends DomainEntity> getType();

  Status getStatus();

  void inProgress();

  void done();

  LocalDateTime getLastChanged();

  void execute(Indexer indexer) throws IndexException;

  boolean canBeDiscarded(int timeout);

  enum Status {
    REQUESTED,
    IN_PROGRESS,
    DONE
  }
}
