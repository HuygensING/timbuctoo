package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.index.request.IndexRequest;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface Indexer {
  void executeFor(IndexRequest request) throws IndexException;

  void executeIndexAction(Class<? extends DomainEntity> type, String id) throws IndexException;
}
