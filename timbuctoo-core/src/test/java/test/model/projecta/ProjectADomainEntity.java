package test.model.projecta;

import test.model.BaseDomainEntity;

public class ProjectADomainEntity extends BaseDomainEntity {

  private String valuea;

  public ProjectADomainEntity() {}

  public ProjectADomainEntity(String id) {
    setId(id);
  }

  public ProjectADomainEntity(String id, String pid, String value1, String value2, String valuea) {
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
