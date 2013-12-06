package test.model;

import java.util.Date;

import nl.knaw.huygens.timbuctoo.model.util.PersonName;

/**
 * Used for testing properties with various types.
 */
public class DomainEntityWithMiscTypes extends BaseDomainEntity {

  private Class<?> type;
  private Date date;
  private PersonName personName;

  public DomainEntityWithMiscTypes() {}

  public DomainEntityWithMiscTypes(String id) {
    setId(id);
  }

  public Class<?> getType() {
    return type;
  }

  public void setType(Class<?> type) {
    this.type = type;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public PersonName getPersonName() {
    return personName;
  }

  public void setPersonName(PersonName personName) {
    this.personName = personName;
  }

}
