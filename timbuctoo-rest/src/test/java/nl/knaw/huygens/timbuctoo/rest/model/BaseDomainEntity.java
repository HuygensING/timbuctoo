package nl.knaw.huygens.timbuctoo.rest.model;

import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

@EntityTypeName("basedomainentities")
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
