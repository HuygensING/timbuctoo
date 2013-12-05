package test.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used for testing {@code Date} properties.
 */
public class DomainEntityWithDates extends BaseDomainEntity {

  private Date sharedDate;
  private Date uniqueDate;

  public DomainEntityWithDates() {}

  public DomainEntityWithDates(String id) {
    setId(id);
  }

  public Date getSharedDate() {
    return sharedDate;
  }

  public void setSharedDate(Date date) {
    sharedDate = date;
  }

  @JsonProperty("^uniqueDate")
  public Date getUniqueDate() {
    return uniqueDate;
  }

  @JsonProperty("^uniqueDate")
  public void setUniqueDate(Date date) {
    uniqueDate = date;
  }

}
