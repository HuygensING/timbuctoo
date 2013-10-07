package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;

import com.fasterxml.jackson.annotation.JsonIgnore;

@IDPrefix("PERS")
public class Person extends DomainEntity {

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
  @IndexAnnotation(fieldName = "dynamic_t_name", isFaceted = false)
  public String getIndexedName() {
    return name.getFullName();
  }

  public PersonName getName() {
    return name;
  }

  public void setName(PersonName name) {
    this.name = name;
  }

  @IndexAnnotation(fieldName = "dynamic_s_birthDate", isFaceted = true, canBeEmpty = true)
  public Datable getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Datable birthDate) {
    this.birthDate = birthDate;
  }

  @IndexAnnotation(fieldName = "dynamic_s_deathDate", isFaceted = true, canBeEmpty = true)
  public Datable getDeathDate() {
    return deathDate;
  }

  public void setDeathDate(Datable deathDate) {
    this.deathDate = deathDate;
  }

}
