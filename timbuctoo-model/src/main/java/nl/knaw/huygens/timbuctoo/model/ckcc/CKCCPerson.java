package nl.knaw.huygens.timbuctoo.model.ckcc;

/*
 * #%L
 * Timbuctoo model
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
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

import java.util.Map;

public class CKCCPerson extends Person {

  /**
   * Unique identifier for concordances
   */
  private String urn;
  /**
   * Either a Pica PPN or a name
   */
  private String cenId;
  private String notes;

  @Override
  public String getIdentificationName() {
    String name = defaultName().getSortName();
    StringBuilder builder = new StringBuilder(name);
    appendPeriod(builder);
    return builder.toString();
  }

  private void appendPeriod(StringBuilder builder) {
    Datable birthDate = getBirthDate();
    Datable deathDate = getDeathDate();

    if (birthDate != null || deathDate != null) {
      builder.append(" (");
      appendDate(builder, birthDate);
      builder.append('-');
      appendDate(builder, deathDate);
      builder.append(')');
    } else if (getFloruit() != null) {
      builder.append(" (").append(getFloruit()).append(')');
    }
  }

  private void appendDate(StringBuilder builder, Datable date) {
    if (date != null) {
      String text = date.toString();
      if (text != null && text.length() != 0) {
        text = text.replaceAll("[<>~?]", "");
        text = text.replaceAll("/", "-");
        int pos = text.indexOf('-');
        if (pos > 0) {
          text = text.substring(0, pos);
        }
        while (text.length() > 1 && text.charAt(0) == '0') {
          text = text.substring(1);
        }
        builder.append(text);
      }
    }
  }

  public String getUrn() {
    return urn;
  }

  public void setUrn(String urn) {
    this.urn = urn;
  }

  public String getCenId() {
    return cenId;
  }

  public void setCenId(String cenId) {
    this.cenId = cenId;
  }

  @JsonIgnore
  public String getCenUrn() {
    if (cenId == null || cenId.isEmpty()) {
      return "CEN::";
    } else if (Character.isDigit(cenId.charAt(0))) {
      // ppn
      return String.format("CEN:%s:", cenId);
    } else {
      // name
      return String.format("CEN::%s", cenId);
    }
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  @Override
  public Map<String, String> getClientRepresentation() {
    Map<String, String> data = Maps.newTreeMap();
    addItemToRepresentation(data, "urn", getUrn());
    addItemToRepresentation(data, "cen", getCenUrn());
    addItemToRepresentation(data, "notes", getNotes());
    return data;
  }

  @Override
  public <T> Map<String, T> createRelSearchRep(Map<String, T> mappedIndexInformation) {
    Map<String, T> filteredMap = Maps.newTreeMap();
    addValueToMap(mappedIndexInformation, filteredMap, "urn");
    addValueToMap(mappedIndexInformation, filteredMap, "cen");
    addValueToMap(mappedIndexInformation, filteredMap, "notes");

    return filteredMap;
  }
}
