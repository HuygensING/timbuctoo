package nl.knaw.huygens.timbuctoo.variation.model.projecta;

import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.variation.model.TestConcreteDoc;

public class ProjectATestDocWithPersonName extends TestConcreteDoc {
  private PersonName personName;

  public PersonName getPersonName() {
    return personName;
  }

  public void setPersonName(PersonName personName) {
    this.personName = personName;
  }
}
