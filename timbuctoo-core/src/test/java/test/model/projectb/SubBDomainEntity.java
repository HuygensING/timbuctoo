package test.model.projectb;

import test.model.BaseDomainEntity;

public class SubBDomainEntity extends BaseDomainEntity {

  private String valueb;

  public SubBDomainEntity() {}

  public SubBDomainEntity(String id) {
    setId(id);
  }

  public SubBDomainEntity(String id, String pid, String value1, String value2, String valueb) {
    setId(id);
    setPid(pid);
    setValue1(value1);
    setValue2(value2);
    setValueb(valueb);
  }

  public String getValueb() {
    return valueb;
  }

  public void setValueb(String valueb) {
    this.valueb = valueb;
  }

}
