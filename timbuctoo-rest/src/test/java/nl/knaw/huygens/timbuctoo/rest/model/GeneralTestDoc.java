package nl.knaw.huygens.timbuctoo.rest.model;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class GeneralTestDoc extends DomainEntity {

  public String name;
  public String generalTestDocValue;

  public GeneralTestDoc() {}

  public GeneralTestDoc(String id) {
    setId(id);
  }

  @Override
  public String getDisplayName() {
    return null;
  }

}
