package test.model.projecta;

import nl.knaw.huygens.timbuctoo.model.Person;

public class ProjectAPerson extends Person {
  private String projectAPersonProperty;

  public String getProjectAPersonProperty() {
    return projectAPersonProperty;
  }

  public void setProjectAPersonProperty(String projectAPersonProperty) {
    this.projectAPersonProperty = projectAPersonProperty;
  }
}
