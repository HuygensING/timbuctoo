package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface Index {

  public void add(Class<? extends DomainEntity> type, String id);
}
