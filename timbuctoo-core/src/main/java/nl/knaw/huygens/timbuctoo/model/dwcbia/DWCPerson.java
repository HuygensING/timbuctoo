package nl.knaw.huygens.timbuctoo.model.dwcbia;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.Person;

public class DWCPerson extends Person {

  private boolean important;

  @IndexAnnotation(fieldName = "dynamic_b_important", isFaceted = true)
  public boolean getImportant() {
    return important;
  }

  public void setImportant(Boolean important) {
    this.important = important;
  }

}
