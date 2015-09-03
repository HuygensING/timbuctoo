package nl.knaw.huygens.timbuctoo;

import nl.knaw.huygens.timbuctoo.model.util.Change;
import test.rest.model.projecta.ProjectADomainEntity;

public class ProjectADomainEntityBuilder {
  private String id;
  private int revision;
  private String pid;
  private Change modified;

  private ProjectADomainEntityBuilder() {

  }

  public static ProjectADomainEntityBuilder aDomainEntity() {
    return new ProjectADomainEntityBuilder();
  }

  public ProjectADomainEntity build() {
    ProjectADomainEntity projectADomainEntity = new ProjectADomainEntity();
    projectADomainEntity.setId(id);
    projectADomainEntity.setPid(pid);
    projectADomainEntity.setRev(revision);
    projectADomainEntity.setModified(modified);

    return projectADomainEntity;
  }

  public ProjectADomainEntityBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public ProjectADomainEntityBuilder withRev(int revision) {
    this.revision = revision;
    return this;
  }

  public ProjectADomainEntityBuilder withModified(Change modified) {
    this.modified = modified;
    return this;
  }

  public ProjectADomainEntityBuilder withPID(String pid) {
    this.pid = pid;
    return this;
  }
}
