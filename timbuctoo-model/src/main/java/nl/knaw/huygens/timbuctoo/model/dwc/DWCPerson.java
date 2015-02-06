package nl.knaw.huygens.timbuctoo.model.dwc;

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

import nl.knaw.huygens.timbuctoo.model.Person;

public class DWCPerson extends Person {

  private String important;
  private String religion; // Zou een (relation) naar een Keyword kunnen worden? Nee!!!
  private String originDb;
  private String dataLine;
  private String scientistBio;

  public String getImportant() {
    return important;
  }

  public void setImportant(String important) {
    this.important = important;
  }

  public void setGender(String gender) {
    setGender(Gender.valueOf(gender));
  }

  public String getReligion() {
    return religion;
  }

  public void setReligion(String religion) {
    this.religion = religion;
  }

  public String getOriginDb() {
    return originDb;
  }

  public void setOriginDb(String origin_db) {
    this.originDb = origin_db;
  }

  public String getDataLine() {
    return dataLine;
  }

  public void setDataLine(String data_line) {
    this.dataLine = data_line;
  }

  public String getScientistBio() {
    return scientistBio;
  }

  public void setScientistBio(String scientist_bio) {
    this.scientistBio = scientist_bio;
  }

}
