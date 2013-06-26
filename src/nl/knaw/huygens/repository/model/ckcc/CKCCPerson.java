package nl.knaw.huygens.repository.model.ckcc;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.model.Person;

@DocumentTypeName("ckccperson")
public class CKCCPerson extends Person {

  /** To start with a simple type: ("f", "m", "?"}, no validation. */
  private String gender;

  @IndexAnnotation(fieldName = "facet_s_gender")
  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

}
