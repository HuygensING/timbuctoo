package nl.knaw.huygens.repository.model.ckcc;

import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.util.Datable;

public class CKCCPerson extends Person {

  /** To start with a simple type: ("f", "m", "?"}, no validation. */
  private String gender;

  @Override
  public String getDisplayName() {
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

  @IndexAnnotation(fieldName = "dynamic_s_gender")
  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

}
