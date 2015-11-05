package nl.knaw.huygens.timbuctoo.model;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.annotations.DBProperty;
import nl.knaw.huygens.timbuctoo.annotations.JsonViews;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TimbuctooTypeIdResolver;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

// Annotations determine to which subclass the entity has to be resolved.
// @see http://wiki.fasterxml.com/JacksonPolymorphicDeserialization
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeIdResolver(value = TimbuctooTypeIdResolver.class)
public abstract class Entity {

  public static final String MODIFIED_PROPERTY_NAME = "^modified";

  private static final String CREATED_PROPERTY_NAME = "^created";

  public static final String REVISION_PROPERTY_NAME = "^rev";

  public static final String ID_PROPERTY_NAME = "_id";

  public static final String DB_ID_PROP_NAME = "tim_id";
  public static final String DB_REV_PROP_NAME = "rev";
  public static final String DB_MOD_PROP_NAME = "modified";

  public static final String INDEX_FIELD_ID = "id";
  public static final String INDEX_FIELD_IDENTIFICATION_NAME = "desc";
  @NotNull
  @Pattern(regexp = Paths.ID_REGEX)
  @JsonProperty(ID_PROPERTY_NAME)
  @DBProperty(value= DB_ID_PROP_NAME, type = FieldType.ADMINISTRATIVE)
  private String id;

  /** Revison number; also used for integrity of updates. */
  @JsonProperty(REVISION_PROPERTY_NAME)
  @DBProperty(value = DB_REV_PROP_NAME, type = FieldType.ADMINISTRATIVE)
  private int rev;

  /** Provides info about creation. */
  @JsonProperty(CREATED_PROPERTY_NAME)
  @DBProperty(value = "created", type = FieldType.ADMINISTRATIVE)
  private Change created;

  /** Provides info about last update. */
  @JsonProperty(MODIFIED_PROPERTY_NAME)
  @DBProperty(value = DB_MOD_PROP_NAME, type = FieldType.ADMINISTRATIVE)
  private Change modified;

  /**
   * Returns the name used for identification of this entity.
   */
  @JsonIgnore
  @IndexAnnotation(fieldName = INDEX_FIELD_IDENTIFICATION_NAME)
  public abstract String getIdentificationName();

  @IndexAnnotation(fieldName = INDEX_FIELD_ID)
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @JsonView(JsonViews.NoExportView.class)
  public int getRev() {
    return rev;
  }

  public void setRev(int rev) {
    this.rev = rev;
  }

  @JsonView(JsonViews.NoExportView.class)
  public Change getCreated() {
    return created;
  }

  public void setCreated(Change created) {
    this.created = created;
  }

  @JsonView(JsonViews.NoExportView.class)
  public Change getModified() {
    return modified;
  }

  public void setModified(Change modified) {
    this.modified = modified;
  }

  /**
   * Normalize this entity; should be called before validation.
   */
  public void normalize(Repository repository) {}

  /**
   * Validation targeted at dependencies between entities.
   */
  public void validateForAdd(Repository repository) throws ValidationException {}

}
