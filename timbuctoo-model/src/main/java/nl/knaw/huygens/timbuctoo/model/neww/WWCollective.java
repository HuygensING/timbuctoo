package nl.knaw.huygens.timbuctoo.model.neww;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.util.List;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.Collective;
import nl.knaw.huygens.timbuctoo.model.RelationRef;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WWCollective extends Collective {

  private String notes;

  // --- temporary fields ------------------------------------------------------

  // For establishing the type of the collective
  public String tempType;
  // For establishing relation with location
  public String tempLocationPlacename;
  // For establishing relation with location
  public String tempOrigin;
  // Redundant
  public String tempShortName;

  // ---------------------------------------------------------------------------

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_location", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = false)
  public List<RelationRef> getLocations() {
    return getRelations("hasLocation");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_member", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = false)
  public List<RelationRef> getMembers() {
    return getRelations("hasMember");
  }

}
