package test.model;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import nl.knaw.huygens.timbuctoo.model.Reference;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used for testing inducing an reducing of non-primitive properties.
 */
public class DomainEntityWithReferences extends BaseDomainEntity {

  private Reference sharedReference;
  private Reference uniqueReference;

  public DomainEntityWithReferences() {}

  public DomainEntityWithReferences(String id) {
    setId(id);
  }

  public Reference getSharedReference() {
    return sharedReference;
  }

  public void setSharedReference(Reference sharedReference) {
    this.sharedReference = sharedReference;
  }

  @JsonProperty("^uniqueReference")
  public Reference getUniqueReference() {
    return uniqueReference;
  }

  @JsonProperty("^uniqueReference")
  public void setUniqueReference(Reference uniqueReference) {
    this.uniqueReference = uniqueReference;
  }

}
