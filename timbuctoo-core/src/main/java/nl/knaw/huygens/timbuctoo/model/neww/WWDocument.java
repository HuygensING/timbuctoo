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

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class WWDocument extends Document {

  private String notes;
  private String origin;
  private List<String> topoi;
  private Source source;

  public String tempCreator;
  public String tempLanguage;
  public List<Print> tempPrints;

  public WWDocument() {
    topoi = Lists.newArrayList();
    tempPrints = Lists.newArrayList();
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
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

  public void addTempPrint(Print print) {
    tempPrints.add(print);
  }

  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  @JsonIgnore
  public boolean isValid() {
    return getTitle() != null;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_library", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = false)
  public List<EntityRef> getLibraries() {
    return getRelations().get("library");
  }

  // ---------------------------------------------------------------------------

  public static class Print {

    private String edition;
    private String publisher;
    private String location;
    private String year;

    public Print() {}

    public Print(String edition, String publisher, String location, String year) {
      setEdition(edition);
      setPublisher(publisher);
      setLocation(location);
      setYear(year);
    }

    public String getEdition() {
      return edition;
    }

    public void setEdition(String edition) {
      this.edition = StringUtils.stripToEmpty(edition);
    }

    public String getPublisher() {
      return publisher;
    }

    public void setPublisher(String publisher) {
      this.publisher = StringUtils.stripToEmpty(publisher);
    }

    public String getLocation() {
      return location;
    }

    public void setLocation(String location) {
      this.location = StringUtils.stripToEmpty(location);
    }

    public String getYear() {
      return year;
    }

    public void setYear(String year) {
      this.year = StringUtils.stripToEmpty(year);
    }

    @Override
    public String toString() {
      return String.format("[%s] [%s] [%s] [%s]", edition, publisher, location, year);
    }

  }

  // ---------------------------------------------------------------------------

  public static class Source {

    private String type;
    private String fullName;
    private String shortName;
    private String notes;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getFullName() {
      return fullName;
    }

    public void setFullName(String fullName) {
      this.fullName = fullName;
    }

    public String getShortName() {
      return shortName;
    }

    public void setShortName(String shortName) {
      this.shortName = shortName;
    }

    public String getNotes() {
      return notes;
    }

    public void setNotes(String notes) {
      this.notes = notes;
    }

  }

}
