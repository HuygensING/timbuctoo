package nl.knaw.huygens.timbuctoo.model;

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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TimbuctooTypeIdResolver;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

// Annotations determine to which subclass the entity has to be resolved.
// @see http://wiki.fasterxml.com/JacksonPolymorphicDeserialization
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeIdResolver(value = TimbuctooTypeIdResolver.class)
public abstract class Entity {

  public static final String ID = "_id";

  @NotNull
  @Pattern(regexp = Paths.ID_REGEX)
  private String id;

  /** Revison number; also used for integrity of updates. */
  private int rev;
  /** Provides info about creation. */
  private Change created;
  /** Provides info about last update. */
  private Change modified;

  /**
   * Returns the name to be displayed for identification of this entity.
   */
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public abstract String getDisplayName();

  @JsonProperty(ID)
  @IndexAnnotation(fieldName = "id")
  public String getId() {
    return id;
  }

  @JsonProperty(ID)
  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("^rev")
  public int getRev() {
    return rev;
  }

  @JsonProperty("^rev")
  public void setRev(int rev) {
    this.rev = rev;
  }

  @JsonProperty("^created")
  public Change getCreated() {
    return created;
  }

  @JsonProperty("^created")
  public void setCreated(Change created) {
    this.created = created;
  }

  @JsonProperty("^modified")
  public Change getModified() {
    return modified;
  }

  @JsonProperty("^modified")
  public void setModified(Change modified) {
    this.modified = modified;
  }

}
