package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import nl.knaw.huygens.timbuctoo.model.util.Change;
import test.model.projecta.SubADomainEntity;

public class SubADomainEntityBuilder {
  private String id;
  private int revision;
  private String pid;
  private Change modified;

  private SubADomainEntityBuilder() {

  }

  public static SubADomainEntityBuilder aDomainEntity() {
    return new SubADomainEntityBuilder();
  }

  public SubADomainEntity build() {
    SubADomainEntity subADomainEntity = new SubADomainEntity();
    subADomainEntity.setId(id);
    subADomainEntity.setPid(pid);
    subADomainEntity.setRev(revision);
    subADomainEntity.setModified(modified);

    return subADomainEntity;
  }

  public SubADomainEntityBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public SubADomainEntityBuilder withRev(int revision) {
    this.revision = revision;
    return this;
  }

  public SubADomainEntityBuilder withAPid() {
    this.pid = "pid";
    return this;
  }

  public SubADomainEntityBuilder withModified(Change modified) {
    this.modified = modified;
    return this;
  }

}
