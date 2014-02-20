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

import com.google.common.collect.Lists;

import nl.knaw.huygens.timbuctoo.model.Person;

public class WWPerson extends Person {

  private String bibliography;
  private String numberOfChildren;
  private List<String> collaborations;

  public String tempBirthPlace;
  public String tempDeath;

  public WWPerson() {
    collaborations = Lists.newArrayList();
  }

  public String[] education;
  public String financial_situation;
  public String[] financials;
  public String[] fs_pseudonyms;
  public String health;
  public String[] languages;
  public String lived_in;
  public String marital_status;
  public String[] memberships;
  public String mother_tongue;
  public String nationality;
  public String notes;
  public String original_field;
  public String personal_situation;
  public String personalSituation;
  public String[] placeOfBirth;
  public String placeOfDeath;
  public String[] professions;
  public String[] ps_children;
  public String[] pseudonyms;
  public String[] publishing_languages;
  public String[] religion;
  public String[] social_class;
  public String spouse;
  public String spouse_id;
  public String type;
  public XURL url;

  public static class XURL {
    public String url;
    public String label;
  }

  public String getBibliography() {
    return bibliography;
  }

  public void setBibliography(String bibliography) {
    this.bibliography = bibliography;
  }

public String getNumberOfChildren() {
	return numberOfChildren;
}

public void setNumberOfChildren(String numberOfChildren) {
	this.numberOfChildren = numberOfChildren;
}

public List<String> getCollaborations() {
	return collaborations;
}

public void setCollaborations(List<String> collaborations) {
	this.collaborations = collaborations;
}

public void addCollaboration(String collaboration) {
	collaborations.add(collaboration);
}

}
