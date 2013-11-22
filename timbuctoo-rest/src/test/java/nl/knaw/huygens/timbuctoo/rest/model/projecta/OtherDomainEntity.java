package nl.knaw.huygens.timbuctoo.rest.model.projecta;

import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

@EntityTypeName("otherdomainentities")
public class OtherDomainEntity extends DomainEntity {

  public String otherThing;

  public OtherDomainEntity() {}

  public OtherDomainEntity(String id) {
    setId(id);
  }

  @Override
  public String getDisplayName() {
    return null;
  }

}
