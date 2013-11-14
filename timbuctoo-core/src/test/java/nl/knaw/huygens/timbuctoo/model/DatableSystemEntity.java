package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.model.util.Datable;

public class DatableSystemEntity extends SystemEntity {

  private Datable testDatable;

  @Override
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  public Datable getTestDatable() {
    return testDatable;
  }

  public void setTestDatable(Datable testDatable) {
    this.testDatable = testDatable;
  }

}
