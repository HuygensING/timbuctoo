package nl.knaw.huygens.timbuctoo.model.dcar;

import nl.knaw.huygens.timbuctoo.model.Person;

public class DCARPerson extends Person {

  private String label;
  private String reference;

  public DCARPerson() {
    label = "";
    reference = "";
  }

  @Override
  public String getDisplayName() {
    return getName().getSortName();
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
