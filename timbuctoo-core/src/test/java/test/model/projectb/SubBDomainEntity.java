package test.model.projectb;

import test.model.BaseDomainEntity;

public class SubBDomainEntity extends BaseDomainEntity {

  private String valueb;

  public SubBDomainEntity() {}

  public SubBDomainEntity(String id) {
    setId(id);
  }

  public SubBDomainEntity(String id, String pid) {
    setId(id);
    setPid(pid);
  }

  public String getValueb() {
    return valueb;
  }

  public void setValueb(String value) {
    valueb = value;
  }

}
