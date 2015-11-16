package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface Indexer {
  void executeIndexAction(Class<? extends DomainEntity> type, String id) throws IndexException;
}
