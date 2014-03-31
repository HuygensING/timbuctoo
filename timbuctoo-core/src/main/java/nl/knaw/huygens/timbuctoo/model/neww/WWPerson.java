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
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.Person;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class WWPerson extends Person {

  private String bibliography;
  private String children;
  private List<String> educations;
  private List<String> financials;
  private List<String> fsPseudonyms;
  private String health;
  private String livedIn;
  private String maritalStatus;
  private String nationality;
  private String notes;
  private String personalSituation;
  private List<String> professions;
  private List<String> religions;
  private List<String> socialClasses;

  // Fields scheduled for removal
  public String tempBirthPlace;
  public String tempChildren;
  public String tempCollaborations; // as relation
  public String tempDeathPlace;
  public String tempDeath;
  public String tempFinancialSituation;
  public String tempLanguages;
  public String tempMemberships; // as relation
  public String tempMotherTongue;
  public String tempName;
  public String tempPlaceOfBirth;
  public String tempPsChildren;
  public String tempPseudonyms;
  public String tempPublishingLanguages;
  public String tempSpouse;

  public WWPerson() {
    educations = Lists.newArrayList();
    financials = Lists.newArrayList();
    fsPseudonyms = Lists.newArrayList();
    professions = Lists.newArrayList();
    religions = Lists.newArrayList();
    socialClasses = Lists.newArrayList();
    setChildren(null); // default
  }

  @Override
  public String getDisplayName() {
    String name = defaultName().getShortName();
    return StringUtils.stripToEmpty(name).isEmpty() ? "[TEMP] " + tempName : name;
  }

  @IndexAnnotation(fieldName = "dynamic_t_bibliography", canBeEmpty = true)
  public String getBibliography() {
    return bibliography;
  }

  public void setBibliography(String bibliography) {
    this.bibliography = bibliography;
  }

  public String getChildren() {
    return children;
  }

  public void setChildren(String value) {
    children = Children.normalize(value);
  }

  @IndexAnnotation(fieldName = "dynamic_t_educations", canBeEmpty = true)
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

  @IndexAnnotation(fieldName = "dynamic_t_financials", canBeEmpty = true)
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

  @IndexAnnotation(fieldName = "dynamic_t_health", canBeEmpty = true)
  public String getHealth() {
    return health;
  }

  public void setHealth(String health) {
    this.health = health;
  }

  public String getLivedIn() {
    return livedIn;
  }

  public void setLivedIn(String livedIn) {
    this.livedIn = livedIn;
  }

  @IndexAnnotation(fieldName = "dynamic_t_maritalstatus", canBeEmpty = true)
  public String getMaritalStatus() {
    return maritalStatus;
  }

  public void setMaritalStatus(String maritalStatus) {
    this.maritalStatus = maritalStatus;
  }

  @IndexAnnotation(fieldName = "dynamic_t_nationality", canBeEmpty = true)
  public String getNationality() {
    return nationality;
  }

  public void setNationality(String nationality) {
    this.nationality = nationality;
  }

  @IndexAnnotation(fieldName = "dynamic_t_notes", canBeEmpty = true)
  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  @IndexAnnotation(fieldName = "dynamic_t_personalsituation", canBeEmpty = true)
  public String getPersonalSituation() {
    return personalSituation;
  }

  public void setPersonalSituation(String personalSituation) {
    this.personalSituation = personalSituation;
  }

  public List<String> getProfessions() {
    return professions;
  }

  @IndexAnnotation(fieldName = "dynamic_t_professions", canBeEmpty = true)
  public void setProfessions(List<String> professions) {
    this.professions = professions;
  }

  public void addProfession(String value) {
    if (value != null) {
      professions.add(value);
    }
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

  @IndexAnnotation(fieldName = "dynamic_t_socialclasses", canBeEmpty = true)
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
    return getRelations().get("hasLanguage");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_collective", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getCollectives() {
    return getRelations().get("isMemberOf");
  }

  // ---------------------------------------------------------------------------

  // Not an enumerated type because of serialization problems.
  public static class Children {
    public static final String UNKNOWN = "UNKNOWN";
    public static final String NO = "NO";
    public static final String YES = "YES";

    public static String normalize(String value) {
      if (NO.equalsIgnoreCase(value)) {
        return NO;
      } else if (YES.equalsIgnoreCase(value)) {
        return YES;
      } else {
        return UNKNOWN;
      }
    }
  }

}
