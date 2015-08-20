package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class IndexRequestFactory {
  public IndexRequestFactory() {

  }

  public IndexRequest forType(Class<? extends DomainEntity> type) {
    return IndexRequestImpl.forType(type);
  }

  public IndexRequestImpl forEntity(Class<? extends DomainEntity> type, String id) {
    return IndexRequestImpl.forEntity(type, id);
  }
}
