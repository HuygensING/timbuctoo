package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class IndexRequestFactory {
  public IndexRequestFactory() {

  }

  public IndexRequest forType(Class<? extends DomainEntity> type) {
    return new CollectionIndexRequest(type);
  }

  public IndexRequest forEntity(Class<? extends DomainEntity> type, String id) {
    return new EntityIndexRequest(type, id);
  }
}
