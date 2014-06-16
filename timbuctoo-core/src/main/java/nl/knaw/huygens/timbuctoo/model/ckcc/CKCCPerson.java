package nl.knaw.huygens.timbuctoo.model.ckcc;

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

import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

public class CKCCPerson extends Person {

  /** Unique identifier for concordances */
  private String urn;
  /** Either a Pica PPN or a name */
  private String cenId;
  private String notes;

  @Override
  public String getDisplayName() {
    return defaultName().getShortName() + period();
  }

  private String period() {
    boolean floruit = false;
    int birthYear = 0;
    int deathYear = 0;

    Datable birthDate = getBirthDate();
    if (birthDate != null) {
      if (birthDate.getFromDate() != null) {
        birthYear = birthDate.getFromYear();
      } else {
        floruit = true;
        if (birthDate.getToDate() != null) {
          birthYear = birthDate.getToYear();
        }
      }
    }

    Datable deathDate = getDeathDate();
    if (deathDate != null) {
      if (deathDate.getToDate() != null) {
        deathYear = deathDate.getToYear();
      } else {
        floruit = true;
        if (deathDate.getFromDate() != null) {
          deathYear = deathDate.getFromYear();
        }
      }
    }

    if (birthYear != 0 || deathYear != 0) {
      StringBuilder builder = new StringBuilder();
      builder.append(" (");
      if (floruit) {
        builder.append("fl. ");
      }
      if (birthYear != 0) {
        builder.append(birthYear);
      }
      if (deathYear > birthYear) {
        builder.append("-");
        if (deathYear != 0) {
          builder.append(deathYear);
        }
      }
      builder.append(")");
      return builder.toString();
    }
    return "";
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

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

}
