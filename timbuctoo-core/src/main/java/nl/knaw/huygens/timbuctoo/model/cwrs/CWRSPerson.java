package nl.knaw.huygens.timbuctoo.model.cwrs;

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

import nl.knaw.huygens.timbuctoo.model.Person;

import com.google.common.collect.Lists;

public class CWRSPerson extends Person {

  private String notes;

  // --- temporary fields ------------------------------------------------------

  public List<String> tempLanguageCodes = Lists.newArrayList();
  public String tempBirthPlace;
  public String tempDeathPlace;
  public String tempNewwId;

  // ---------------------------------------------------------------------------

  @Override
  public String getDisplayName() {
    return defaultName().getShortName();
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

}
