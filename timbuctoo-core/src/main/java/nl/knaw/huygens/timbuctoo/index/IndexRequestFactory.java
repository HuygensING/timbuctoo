package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class IndexRequestFactory {
  public IndexRequestFactory() {

  }

  public IndexRequest forType(Class<? extends DomainEntity> type) {
    return IndexRequest.forType(type);
  }

  public IndexRequest forEntity(Class<? extends DomainEntity> type, String id) {
    return IndexRequest.forEntity(type, id);
  }
}
