package test.model;

import java.util.Date;

/**
 * Used for testing properties with various types.
 */
public class DomainEntityWithMiscTypes extends BaseDomainEntity {

  private Class<?> type;
  private Date date;

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

}
