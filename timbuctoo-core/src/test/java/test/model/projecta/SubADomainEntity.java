package test.model.projecta;

import test.model.BaseDomainEntity;

public class SubADomainEntity extends BaseDomainEntity {

  private String valuea;

  public SubADomainEntity() {}

  public SubADomainEntity(String id) {
    setId(id);
  }

  public SubADomainEntity(String id, String pid) {
    setId(id);
    setPid(pid);
  }

  public SubADomainEntity(String id, String pid, String value1, String value2, String valuea) {
    setId(id);
    setPid(pid);
    setValue1(value1);
    setValue2(value2);
    setValuea(valuea);
  }

  public String getValuea() {
    return valuea;
  }

  public void setValuea(String valuea) {
    this.valuea = valuea;
  }

}
