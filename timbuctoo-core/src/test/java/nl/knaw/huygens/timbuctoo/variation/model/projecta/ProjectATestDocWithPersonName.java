package nl.knaw.huygens.timbuctoo.variation.model.projecta;

import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.variation.model.TestConcreteDoc;

import com.google.common.base.Objects;

public class ProjectATestDocWithPersonName extends TestConcreteDoc {
  private PersonName personName;

  public PersonName getPersonName() {
    return personName;
  }

  public void setPersonName(PersonName personName) {
    this.personName = personName;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ProjectATestDocWithPersonName)) {
      return false;
    }
    ProjectATestDocWithPersonName other = (ProjectATestDocWithPersonName) obj;

    return Objects.equal(other.personName, personName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(personName);
  }

  @Override
  public String toString() {
    return "ProjectATestDocWithPersonName{\npersonName: " + personName + "\n}";
  }
}
