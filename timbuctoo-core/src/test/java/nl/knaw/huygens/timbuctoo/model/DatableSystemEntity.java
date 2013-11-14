package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.model.util.Datable;

import com.google.common.base.Objects;

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

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DatableSystemEntity)) {
      return false;
    }

    DatableSystemEntity other = (DatableSystemEntity) obj;

    boolean isEqual = true;
    isEqual &= Objects.equal(other.testDatable, testDatable);
    isEqual &= Objects.equal(other.getRev(), getRev());
    isEqual &= Objects.equal(other.isDeleted(), isDeleted());

    return isEqual;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DatableSystemEntity{\ntestDatable: ");
    sb.append(testDatable);
    sb.append("\nrev: ");
    sb.append(getRev());
    sb.append("\ndeleted: ");
    sb.append(isDeleted());
    sb.append("\n}");

    return sb.toString();
  }
}
