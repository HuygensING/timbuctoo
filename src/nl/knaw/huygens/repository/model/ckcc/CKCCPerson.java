package nl.knaw.huygens.repository.model.ckcc;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.util.Datable;

@DocumentTypeName("ckccperson")
public class CKCCPerson extends Person {

  /** To start with a simple type: ("f", "m", "?"}, no validation. */
  private String gender;

  @Override
  public String getDescription() {
    return String.format("%s (%s-%s)", getName().getSortName(), year(getBirthDate()), year(getDeathDate()));
  }

  private String year(Datable datable) {
    if (datable == null) {
      return "?";
    }
    String value = datable.toString();
    if (value.matches("^\\d\\d\\d\\d.*?$")) {
      return value.substring(0, 4);
    }
    if (value.matches("^.*?\\d\\d\\d\\d$")) {
      return value.substring(value.length() - 4);
    }
    return value;
  }

  @IndexAnnotation(fieldName = "facet_s_gender")
  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

}
