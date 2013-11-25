package nl.knaw.huygens.timbuctoo.rest.model;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class BaseDomainEntity extends DomainEntity {

  public String name;
  public String generalTestDocValue;

  public BaseDomainEntity() {}

  public BaseDomainEntity(String id) {
    setId(id);
  }

  @Override
  public String getDisplayName() {
    return null;
  }

}
