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

import static nl.knaw.huygens.timbuctoo.model.neww.RelTypeNames.IS_CREATOR_OF;
import static nl.knaw.huygens.timbuctoo.model.neww.RelTypeNames.IS_LANGUAGE_OF;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

/**
 * Contains the identified core fields.
 * However, creators and languages are implemented as relation.
 * Relations must be modelled better. E.g. where to put definitions, which restrictions apply?
 */
@IDPrefix("DOCU")
public class Document extends DomainEntity {

  // 'Anthology', 'Article', 'Award', 'Catalogue', 'Compilation', 'Diary', 'Letter',
  // 'List', 'Periodical', 'Publicity', 'Sheetmusic', 'Theater Script', 'Work' (ROLE?)
  private String type;
  private String title;
  private String description;
  private Datable date;

  @Override
  public String getDisplayName() {
    return getTitle();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Datable getDate() {
    return date;
  }

  public void setDate(Datable date) {
    this.date = date;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_creator", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = false)
  public List<EntityRef> getCreators() {
    return getRelations().get(IS_CREATOR_OF.inverse);
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_language", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = false)
  public List<EntityRef> getLanguages() {
    return getRelations().get(IS_LANGUAGE_OF.inverse);
  }

}
