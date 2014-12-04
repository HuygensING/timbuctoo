package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class DomainEntityWithoutOAIDublinCoreFields extends DomainEntity {
  private String test;

  @Override
  public String getIdentificationName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getTest() {
    return test;
  }

  public void setTest(String test) {
    this.test = test;
  }

}
