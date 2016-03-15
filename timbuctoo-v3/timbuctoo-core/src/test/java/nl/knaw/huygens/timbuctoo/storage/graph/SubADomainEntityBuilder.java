package nl.knaw.huygens.timbuctoo.storage.graph;

/*
 * #%L
 * Timbuctoo core
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
import test.model.projecta.SubADomainEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;

public class SubADomainEntityBuilder {
  private String id;
  private int revision;
  private String pid;
  private Change modified;
  private String sharedValue;
  private List<String> variations;

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
    subADomainEntity.setSharedValue(sharedValue);
    subADomainEntity.setVariations(variations);


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

  public SubADomainEntityBuilder withSharedValue(String sharedValue) {
    this.sharedValue = sharedValue;
    return this;
  }

  public SubADomainEntityBuilder withVariations(Class<?>... types) {
    this.variations = Arrays.stream(types).map(type -> getInternalName(type)).collect(Collectors.toList());
    return this;
  }
}
