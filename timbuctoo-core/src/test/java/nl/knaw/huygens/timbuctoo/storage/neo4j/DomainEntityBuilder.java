package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.util.Change;
import test.model.projecta.SubADomainEntity;

public class DomainEntityBuilder {
  private String id;
  private int revision;
  private String pid;
  private Change modified;

  private DomainEntityBuilder() {

  }

  public static DomainEntityBuilder aDomainEntity() {
    return new DomainEntityBuilder();
  }

  public SubADomainEntity build() {
    SubADomainEntity subADomainEntity = new SubADomainEntity();
    subADomainEntity.setId(id);
    subADomainEntity.setPid(pid);
    subADomainEntity.setRev(revision);
    subADomainEntity.setModified(modified);

    return subADomainEntity;
  }

  public DomainEntityBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public DomainEntityBuilder withRev(int revision) {
    this.revision = revision;
    return this;
  }

  public DomainEntityBuilder withAPid() {
    this.pid = "pid";
    return this;
  }

  public DomainEntityBuilder withModified(Change modified) {
    this.modified = modified;
    return this;
  }
}
