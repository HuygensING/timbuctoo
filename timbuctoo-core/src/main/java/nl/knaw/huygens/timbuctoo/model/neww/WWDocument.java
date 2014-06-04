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
import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.RelationEntityRef;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.util.Text;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class WWDocument extends Document {

  private boolean source;
  private String notes;
  private List<String> topoi;
  private String englishTitle;

  // --- temporary fields ------------------------------------------------------

  public String tempOldId; // record id in NEWW database
  public String tempCreator;
  public String tempLanguage;
  public String tempOrigin;

  // ---------------------------------------------------------------------------

  public WWDocument() {
    source = false;
    topoi = Lists.newArrayList();
  }

  @Override
  public String getDisplayName() {
    StringBuilder builder = new StringBuilder();
    List<EntityRef> refs = getRelations("isCreatedBy");
    if (refs != null) {
      for (EntityRef ref : refs) {
        RelationEntityRef rref = (RelationEntityRef) ref;
        String author = rref.getDisplayName().replace("[TEMP] ", "");
        Text.appendTo(builder, author, "; ");
      }
    }
    Text.appendTo(builder, getTitle(), " - ");
    Datable d = getDate();
    if (d != null) {
      int year = d.getFromYear();
      builder.append(String.format(" (%d)", year));
    }
    return builder.toString();
  }

  public boolean isSource() {
    return source;
  }

  public void setSource(boolean source) {
    this.source = source;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public List<String> getTopoi() {
    return topoi;
  }

  public void setTopoi(List<String> topoi) {
    this.topoi = topoi;
  }

  public void addTopos(String topos) {
    if (topos != null) {
      topoi.add(topos);
    }
  }

  @JsonIgnore
  public boolean isValid() {
    return getTitle() != null;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_genre", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getGenres() {
    return getRelations("hasGenre");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_library", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = false)
  public List<EntityRef> getLibraries() {
    // Relation with collectives with type "library".
    return getRelations("isStoredAt");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_origin", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getOrigins() {
    return getRelations("hasPublishLocation");
  }

  public String getEnglishTitle() {
    return englishTitle;
  }

  public void setEnglishTitle(String englishTitle) {
    this.englishTitle = englishTitle;
  }

}
