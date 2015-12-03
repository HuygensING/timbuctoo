package nl.knaw.huygens.timbuctoo;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
