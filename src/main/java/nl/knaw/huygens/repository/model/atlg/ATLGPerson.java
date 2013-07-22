package nl.knaw.huygens.repository.model.atlg;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.model.Person;

@DocumentTypeName("atlgperson")
public class ATLGPerson extends Person {

  private String label;
  private String reference;

  public ATLGPerson() {
    label = "";
    reference = "";
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

}
