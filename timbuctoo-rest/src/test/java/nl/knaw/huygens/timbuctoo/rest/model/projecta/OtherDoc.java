package nl.knaw.huygens.timbuctoo.rest.model.projecta;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class OtherDoc extends DomainEntity {

  public String otherThing;

  public OtherDoc() {}

  public OtherDoc(String id) {
    setId(id);
  }

  @Override
  public String getDisplayName() {
    return null;
  }

}
