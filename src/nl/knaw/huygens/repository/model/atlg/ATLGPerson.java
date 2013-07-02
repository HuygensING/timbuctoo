package nl.knaw.huygens.repository.model.atlg;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.util.PersonName;
import nl.knaw.huygens.repository.model.util.PersonNameComponentType;

import org.apache.commons.lang.StringUtils;

@DocumentTypeName("atlgperson")
public class ATLGPerson extends Person {

  public PersonName personName;

  private String label;
  private String reference;

  public ATLGPerson() {
    personName = new PersonName();
    label = "";
    reference = "";
  }

  public void addNameComponent(PersonNameComponentType type, String value) {
    personName.addNameComponent(type, value);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setLabel(String[] labels) {
    this.label = StringUtils.join(labels, "; ");
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public void setReference(String[] references) {
    this.reference = StringUtils.join(references, "; ");
  }

}
