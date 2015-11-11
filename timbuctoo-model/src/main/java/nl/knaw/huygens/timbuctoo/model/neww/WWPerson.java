package nl.knaw.huygens.timbuctoo.model.neww;

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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DerivedRelationDescription;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.mapping.VirtualProperty;
import nl.knaw.huygens.timbuctoo.oaipmh.DublinCoreMetadataField;
import nl.knaw.huygens.timbuctoo.oaipmh.OAIDublinCoreField;
import nl.knaw.huygens.timbuctoo.util.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class WWPerson extends Person {

  private String bibliography;
  private String children;
  private List<String> fsPseudonyms;
  private String health;
  private String livedIn;
  private String nationality;
  private String notes;
  private String personalSituation;

  // --- temporary fields ------------------------------------------------------

  /**
   * Identification of record in NEWW database.
   * Used by COBWWEB services for linking records.
   */
  public String tempOldId;

  public String tempBirthPlace;
  public String tempChildren;
  public String tempCollaborations; // as relation
  public String tempDeathPlace;
  public String tempDeath;
  public String tempFinancialSituation;
  public String tempMemberships; // as relation
  public String tempMotherTongue;
  private String tempName;
  public String tempPlaceOfBirth;
  public String tempPsChildren;
  public String tempPseudonyms;
  private String tempSpouse;

  // ---------------------------------------------------------------------------

  public WWPerson() {
    fsPseudonyms = Lists.newArrayList();
    setChildren(null); // default
  }

  @Override
  @VirtualProperty(propertyName = "name")
  public String getIdentificationName() {
    String name = defaultName().getShortName();
    return StringUtils.stripToEmpty(name).isEmpty() ? "[TEMP] " + tempName : name;
  }

  public String getBibliography() {
    return bibliography;
  }

  public void setBibliography(String bibliography) {
    this.bibliography = bibliography;
  }

  @IndexAnnotation(fieldName = "dynamic_s_children", canBeEmpty = true, isFaceted = true)
  public String getChildren() {
    return children;
  }

  public void setChildren(String value) {
    children = Children.normalize(value);
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

  public String getLivedIn() {
    return livedIn;
  }

  public void setLivedIn(String livedIn) {
    this.livedIn = livedIn;
  }

  @OAIDublinCoreField(dublinCoreField = DublinCoreMetadataField.TITLE)
  @Override
  public String getIndexedName() {
    String name = super.getIndexedName();
    return (!StringUtils.isBlank(name)) ? name : getTempName();
  }

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

  public String getPersonalSituation() {
    return personalSituation;
  }

  public void setPersonalSituation(String personalSituation) {
    this.personalSituation = personalSituation;
  }

  public String getTempName() {
    return tempName;
  }

  public void setTempName(String tempName) {
    this.tempName = tempName;
  }

  // Indexed for curation phase only
  @IndexAnnotation(fieldName = "dynamic_t_tempspouse", canBeEmpty = true)
  public String getTempSpouse() {
    return tempSpouse;
  }

  public void setTempSpouse(String tempSpouse) {
    this.tempSpouse = tempSpouse;
  }


  // a facet that allows searching all the locations related to person.
  @IndexAnnotation(fieldName = "dynamic_s_relatedLocations", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getRelatedLocations() {
    List<RelationRef> relatedLocations = Lists.newArrayList();

    List<RelationRef> residenceLocations = getResidenceLocation();
    if (residenceLocations != null) {
      relatedLocations.addAll(residenceLocations);
    }

    List<RelationRef> birthPlaces = getBirthPlace();
    if (birthPlaces != null) {
      relatedLocations.addAll(birthPlaces);
    }

    List<RelationRef> deathPlaces = getDeathPlace();
    if (deathPlaces != null) {
      relatedLocations.addAll(deathPlaces);
    }

    return relatedLocations;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_residence", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  @VirtualProperty(propertyName = "residenceLocation")
  public List<RelationRef> getResidenceLocation() {
    return getRelations("hasResidenceLocation");
  }

  @JsonIgnore
  @OAIDublinCoreField(dublinCoreField = DublinCoreMetadataField.DESCRIPTION)
  public String getCountries() {
    StringBuilder sb = new StringBuilder();

    for (RelationRef relLocation : getRelatedLocations()) {
      Text.appendTo(sb, relLocation.getDisplayName(), " ");
    }

    return sb.toString();
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_language", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getPrimaryLanguages() {
    return getRelations("hasPersonLanguage");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_collective", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getCollectives() {
    return getRelations("isMemberOf");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_religion", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getReligions() {
    return getRelations("hasReligion");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_marital_status", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getMaritalStatuses() {
    return getRelations("hasMaritalStatus");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_social_class", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getSocialClasses() {
    return getRelations("hasSocialClass");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_education", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getEducations() {
    return getRelations("hasEducation");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_profession", accessors = {"getDisplayName"}, isFaceted = true, canBeEmpty = true)
  public List<RelationRef> getProfessions() {
    return getRelations("hasProfession");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_financials", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getFinancials() {
    return getRelations("hasFinancialSituation");
  }

  // ---------------------------------------------------------------------------
  private static final DerivedRelationDescription PERSON_LANGUAGE = new DerivedRelationDescription("hasPersonLanguage", "isCreatorOf", "hasWorkLanguage");

  private static final List<DerivedRelationDescription> DERIVED_RELATION_TYPES = ImmutableList.of(PERSON_LANGUAGE);


  @Override
  public List<DerivedRelationDescription> getDerivedRelationDescriptions() {
    return DERIVED_RELATION_TYPES;
  }

  // ---------------------------------------------------------------------------

  @Override
  public Map<String, String> getClientRepresentation() {
    Map<String, String> data = Maps.newTreeMap();
    addItemToRepresentation(data, "id", getId());
    String name = defaultName().getShortName();
    addItemToRepresentation(data, "name", StringUtils.stripToEmpty(name).isEmpty() ? getTempName() : name);
    addItemToRepresentation(data, "gender", getGender());
    addItemToRepresentation(data, "birthDate", getBirthDate() != null ? getBirthDate().getFromYear() : null);
    addItemToRepresentation(data, "deathDate", getDeathDate() != null ? getDeathDate().getFromYear() : null);
    addRelationToRepresentation(data, "residenceLocation", "hasResidenceLocation");
    return data;
  }

  @Override
  public <T> Map<String, T> createRelSearchRep(Map<String, T> mappedIndexInformation) {
    Map<String, T> filteredMap = Maps.newTreeMap();

    addValueToMap(mappedIndexInformation, filteredMap, ID_PROPERTY_NAME);
    addValueToMap(mappedIndexInformation,filteredMap, "name");
    addValueToMap(mappedIndexInformation,filteredMap, "gender");
    addValueToMap(mappedIndexInformation,filteredMap, "birthDate");
    addValueToMap(mappedIndexInformation,filteredMap, "deathDate");
    addValueToMap(mappedIndexInformation,filteredMap, "residenceLocation");

    return filteredMap;
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
