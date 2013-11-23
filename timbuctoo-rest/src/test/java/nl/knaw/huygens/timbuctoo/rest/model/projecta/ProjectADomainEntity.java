package nl.knaw.huygens.timbuctoo.rest.model.projecta;

import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.rest.model.BaseDomainEntity;

@EntityTypeName("projectadomainentities")
public class ProjectADomainEntity extends BaseDomainEntity {

  public String projectAGeneralTestDocValue;

  public ProjectADomainEntity() {}

  public ProjectADomainEntity(String id) {
    super(id);
  }

}
