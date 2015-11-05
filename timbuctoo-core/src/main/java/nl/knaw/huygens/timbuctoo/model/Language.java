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

import nl.knaw.huygens.timbuctoo.annotations.DBProperty;
import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.annotations.RawSearchField;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;

/**
 * Denotes a language.
 */
@IDPrefix("LANG")
@RawSearchField(Language.INDEX_FIELD_NAME)
public class Language extends DomainEntity {

  static final String INDEX_FIELD_NAME = "dynamic_t_name";

  public static final String CODE = "^code";

  /** Unique code (ISO-639-3). */
  @DBProperty(value = "code", type = FieldType.ADMINISTRATIVE)
  private String code;
  /** English name. */
  private String name;
  /** Is this a core language? */
  private boolean core;

  public Language() {
    core = false;
  }

  @Override
  public String getIdentificationName() {
    return getName();
  }

  @JsonProperty(CODE)
  public String getCode() {
    return code;
  }

  @JsonProperty(CODE)
  public void setCode(String code) {
    this.code = code;
  }

  @IndexAnnotation(fieldName = INDEX_FIELD_NAME, isFaceted = false)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    setDisplayName(name);
  }

  public boolean isCore() {
    return core;
  }

  public void setCore(boolean core) {
    this.core = core;
  }

}
