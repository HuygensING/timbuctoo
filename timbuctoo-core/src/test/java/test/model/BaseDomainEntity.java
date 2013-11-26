package test.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

/**
 * A domain entity for test purposes, in particular for handling cases
 * where properties are set and modified. The type of these properties
 * is not relevant for that purpose, so we simply use strings.
 */
@IDPrefix("TDOM")
public class BaseDomainEntity extends DomainEntity {

  private String value1;
  private String value2;

  public BaseDomainEntity() {}

  public BaseDomainEntity(String id) {
    setId(id);
  }

  public BaseDomainEntity(String id, String pid, String value1, String value2) {
    setId(id);
    setPid(pid);
    setValue1(value1);
    setValue2(value2);
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  public String getValue1() {
    return value1;
  }

  public void setValue1(String value1) {
    this.value1 = value1;
  }

  public String getValue2() {
    return value2;
  }

  public void setValue2(String value2) {
    this.value2 = value2;
  }

}
