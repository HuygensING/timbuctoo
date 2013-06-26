package nl.knaw.huygens.repository.model.dwcbia;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.Person;

@DocumentTypeName("dwcperson")
public class DWCPerson extends Person {

  private boolean important;

  @IndexAnnotation(fieldName = "facet_b_important", isFaceted = true)
  public boolean getImportant() {
    return important;
  }

  public void setImportant(Boolean important) {
    this.important = important;
  }

}
