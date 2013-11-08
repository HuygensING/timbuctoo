package nl.knaw.huygens.timbuctoo.variation.model;

import nl.knaw.huygens.timbuctoo.model.Role;

import com.google.common.base.Objects;

public class NewTestRole extends Role {
  private String newTestRoleName;

  public String getNewTestRoleName() {
    return newTestRoleName;
  }

  public void setNewTestRoleName(String newTestRoleName) {
    this.newTestRoleName = newTestRoleName;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NewTestRole)) {
      return false;
    }

    NewTestRole other = (NewTestRole) obj;

    return Objects.equal(other.newTestRoleName, newTestRoleName);
  }

  @Override
  public String toString() {
    return "NewTestRole{\nnewTestRoleName: " + newTestRoleName + "\n}";
  }

}
