package test.model;

/**
 * Used for testing properties with various types.
 */
public class DomainEntityWithMiscTypes extends BaseDomainEntity {

  private Class<?> type;

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

}
