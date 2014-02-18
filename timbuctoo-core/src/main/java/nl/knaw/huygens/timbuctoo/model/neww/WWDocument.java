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

import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.util.Link;

import com.google.common.collect.Lists;

public class WWDocument extends Document {

  private String notes;
  private String reference;
  private String origin;
  private Link url;
  private List<Print> prints;

  public String tempCreator;
  public String tempLanguage;

  public WWDocument() {
    prints = Lists.newArrayList();
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  public Link getUrl() {
    return url;
  }

  public void setUrl(Link url) {
    this.url = url;
  }

  public List<Print> getPrints() {
    return prints;
  }

  public void setPrints(List<Print> prints) {
    this.prints = prints;
  }

  public void addPrint(Print print) {
    prints.add(print);
  }

}
