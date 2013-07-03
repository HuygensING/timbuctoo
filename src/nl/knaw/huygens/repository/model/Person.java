package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.model.util.Datable;
import nl.knaw.huygens.repository.model.util.PersonName;

import com.fasterxml.jackson.annotation.JsonIgnore;

@IDPrefix("PER")
@DocumentTypeName("person")
public class Person extends DomainDocument {

  private PersonName name;
  private Datable birthDate;
  private Datable deathDate;

  public Person() {
    name = new PersonName();
  }

  @Override
  public String getDisplayName() {
    return name.getShortName();
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "facet_t_name", isFaceted = true)
  public String getIndexedName() {
    return name.getFullName();
  }

  public PersonName getName() {
    return name;
  }

  public void setName(PersonName name) {
    this.name = name;
  }

  @IndexAnnotation(fieldName = "facet_s_birthDate", isFaceted = true, canBeEmpty = true)
  public Datable getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Datable birthDate) {
    this.birthDate = birthDate;
  }

  @IndexAnnotation(fieldName = "facet_s_deathDate", isFaceted = true, canBeEmpty = true)
  public Datable getDeathDate() {
    return deathDate;
  }

  public void setDeathDate(Datable deathDate) {
    this.deathDate = deathDate;
  }

}
