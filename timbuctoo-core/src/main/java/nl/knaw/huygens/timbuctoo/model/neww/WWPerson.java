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

import static nl.knaw.huygens.timbuctoo.model.neww.RelTypeNames.LANGUAGE_OF;

import java.util.List;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.Person;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class WWPerson extends Person {

  private String bibliography;
  private int numberOfChildren;
  private List<String> collaborations;
  private List<String> educations;
  private List<String> financials;
  private List<String> fsPseudonyms;
  private String health;
  private String livedIn;
  private String maritalStatus;
  private List<String> memberships;
  private String nationality;
  private String notes;
  private String personalSituation;
  private String psChildren;
  private List<String> professions;
  private List<String> religions;
  private List<String> socialClasses;

  // Fields scheduled for removal
  public String tempBirthPlace;
  public String tempDeathPlace;
  public String tempDeath;
  public String tempFinancialSituation;
  public List<String> tempLanguages = Lists.newArrayList();
  public String tempMotherTongue;
  public String tempName;
  public List<String> tempPlaceOfBirth = Lists.newArrayList();
  public List<String> tempPseudonyms = Lists.newArrayList();
  public List<String> tempPublishingLanguages = Lists.newArrayList();
  public String tempSpouse;

  public WWPerson() {
    collaborations = Lists.newArrayList();
    educations = Lists.newArrayList();
    financials = Lists.newArrayList();
    fsPseudonyms = Lists.newArrayList();
    memberships = Lists.newArrayList();
    professions = Lists.newArrayList();
    religions = Lists.newArrayList();
    socialClasses = Lists.newArrayList();
  }

  @Override
  public String getDisplayName() {
    String name = getName().getShortName();
    return StringUtils.stripToEmpty(name).isEmpty() ? "[TEMP] " + tempName : name;
  }

  public String getBibliography() {
    return bibliography;
  }

  public void setBibliography(String bibliography) {
    this.bibliography = bibliography;
  }

  public int getNumberOfChildren() {
    return numberOfChildren;
  }

  public void setNumberOfChildren(int numberOfChildren) {
    this.numberOfChildren = numberOfChildren;
  }

  public List<String> getCollaborations() {
    return collaborations;
  }

  public void setCollaborations(List<String> collaborations) {
    this.collaborations = collaborations;
  }

  public void addCollaboration(String value) {
    if (value != null) {
      collaborations.add(value);
    }
  }

  public List<String> getEducations() {
    return educations;
  }

  public void setEducations(List<String> educations) {
    this.educations = educations;
  }

  public void addEducation(String value) {
    if (value != null) {
      educations.add(value);
    }
  }

  public List<String> getFinancials() {
    return financials;
  }

  public void setFinancials(List<String> financials) {
    this.financials = financials;
  }

  public void addFinancial(String value) {
    if (value != null) {
      financials.add(value);
    }
  }

  public List<String> getFsPseudonyms() {
    return fsPseudonyms;
  }

  public void setFsPseudonyms(List<String> fsPseudonyms) {
    this.fsPseudonyms = fsPseudonyms;
  }

  public void addFsPseudonym(String value) {
    if (value != null) {
      fsPseudonyms.add(value);
    }
  }

  public String getHealth() {
    return health;
  }

  public void setHealth(String health) {
    this.health = health;
  }

  public void addTempLanguage(String value) {
    if (value != null) {
      tempLanguages.add(value);
    }
  }

  public String getLivedIn() {
    return livedIn;
  }

  public void setLivedIn(String livedIn) {
    this.livedIn = livedIn;
  }

  public String getMaritalStatus() {
    return maritalStatus;
  }

  public void setMaritalStatus(String maritalStatus) {
    this.maritalStatus = maritalStatus;
  }

  public List<String> getMemberships() {
    return memberships;
  }

  public void setMemberships(List<String> memberships) {
    this.memberships = memberships;
  }

  public void addMembership(String value) {
    memberships.add(value);
  }

  public String getNationality() {
    return nationality;
  }

  public void setNationality(String nationality) {
    this.nationality = nationality;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getPersonalSituation() {
    return personalSituation;
  }

  public void setPersonalSituation(String personalSituation) {
    this.personalSituation = personalSituation;
  }

  public List<String> getProfessions() {
    return professions;
  }

  public void setProfessions(List<String> professions) {
    this.professions = professions;
  }

  public void addProfession(String value) {
    if (value != null) {
      professions.add(value);
    }
  }

  public String getPsChildren() {
    return psChildren;
  }

  public void setPsChildren(String psChildren) {
    this.psChildren = psChildren;
  }

  public List<String> getReligions() {
    return religions;
  }

  public void setReligions(List<String> religions) {
    this.religions = religions;
  }

  public void addReligion(String value) {
    if (value != null) {
      religions.add(value);
    }
  }

  public List<String> getSocialClasses() {
    return socialClasses;
  }

  public void setSocialClasses(List<String> socialClasses) {
    this.socialClasses = socialClasses;
  }

  public void addSocialClass(String value) {
    if (value != null) {
      socialClasses.add(value);
    }
  }

  // NOTE. Some relations are generic, but a project need not be interested
  // So it seems to make sense to define relations here and not in Person

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_language", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getPrimaryLanguages() {
    return getRelations().get(LANGUAGE_OF.inverse);
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_collective", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getCollectives() {
    return getRelations().get("is_member_of");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_birthplace", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getBirthPlace() {
    return getRelations().get("has_birth_place");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_deathplace", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getDeathPlace() {
    return getRelations().get("has_death_place");
  }

}
