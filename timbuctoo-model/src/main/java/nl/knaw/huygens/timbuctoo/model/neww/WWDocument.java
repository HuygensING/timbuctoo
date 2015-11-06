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
import nl.knaw.huygens.facetedsearch.model.FacetType;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DerivedProperty;
import nl.knaw.huygens.timbuctoo.model.DerivedRelationDescription;
import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.oaipmh.DublinCoreMetadataField;
import nl.knaw.huygens.timbuctoo.oaipmh.OAIDublinCoreField;
import nl.knaw.huygens.timbuctoo.util.Text;

import java.util.List;
import java.util.Map;

public class WWDocument extends Document {

  public static final String TYPE = "type";
  public static final String DATE = "date";
  public static final String GENRE = "genre";
  public static final String LANGUAGE = "language";
  private boolean source;
  private String notes;
  private List<String> topoi;
  private String englishTitle;

  // --- temporary fields ------------------------------------------------------

  /**
   * Identification of record in NEWW database.
   * Used by COBWWEB services for linking records.
   */
  public String tempOldId;

  public String tempCreator;
  public String tempLanguage;
  public String tempOrigin;

  // ---------------------------------------------------------------------------

  public WWDocument() {
    source = false;
    topoi = Lists.newArrayList();
  }

  @Override
  public String getIdentificationName() {
    StringBuilder builder = new StringBuilder();
    for (RelationRef ref : getRelations("isCreatedBy")) {
      Text.appendTo(builder, ref.getDisplayName(), "; ");
    }
    Text.appendTo(builder, getTitle(), " - ");
    if (getDate() != null) {
      int year = getDate().getFromYear();
      builder.append(String.format(" (%d)", year));
    }
    return builder.toString();
  }

  @IndexAnnotation(fieldName = "dynamic_b_is_source", facetType = FacetType.BOOLEAN, isFaceted = true)
  public boolean isSource() {
    return source;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_sources", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getSources() {
    return getRelations("hasDocumentSource");
  }

  public void setSource(boolean source) {
    this.source = source;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
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

  @JsonIgnore
  public boolean isValid() {
    return getTitle() != null;
  }

  // used for the generation of CMDI data.
  @JsonIgnore
  public List<RelationRef> getOrigins() {
    return getRelations("hasPublishLocation");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_firstPublisher", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public RelationRef getFirstPublisher() {
    WWRelationRef firstPublisher = null;

    for (RelationRef publisher : getRelations("isPublishedBy")) {
      if (publisher instanceof WWRelationRef) {
        WWRelationRef wwPublisher = (WWRelationRef) publisher;
        if (wwPublisher.getDate() != null && (firstPublisher == null || wwPublisher.getDate().compareTo(firstPublisher.getDate()) < 0)) {
          firstPublisher = wwPublisher;
        }
      }
    }

    return firstPublisher;
  }

  public String getEnglishTitle() {
    return englishTitle;
  }

  public void setEnglishTitle(String englishTitle) {
    this.englishTitle = englishTitle;
  }

  // ---------------------------------------------------------------------------

  private static final DerivedProperty AUTHOR_NAME = new DerivedProperty("authorName", "isCreatedBy", "getIndexedName", "getAuthorName");
  private static final DerivedProperty AUTHOR_GENDER = new DerivedProperty("authorGender", "isCreatedBy", "getGender", "getAuthorGender");
  private static final DerivedProperty AUTHOR_BIRTH_DATE = new DerivedProperty("authorBirthDate", "isCreatedBy", "getBirthDate", "getAuthorBirthDate");
  private static final DerivedProperty AUTHOR_DEATH_DATE = new DerivedProperty("authorDeathDate", "isCreatedBy", "getDeathDate", "getAuthorDeathDate");
  private static final DerivedProperty AUTHOR_CHILDREN = new DerivedProperty("authorChildren", "isCreatedBy", "getChildren", "getAuthorChildren");
  private static final DerivedProperty AUTHOR_TYPE = new DerivedProperty("authorTypes", "isCreatedBy", "getTypes", "getAuthorTypes");
  private static final DerivedProperty GENRES = new DerivedProperty("genre", "hasGenre", "getIdentificationName", "getGenres");
  private static final DerivedProperty LIBRARIES = new DerivedProperty("library", "isStoredAt", "getIdentificationName", "getLibraries");
  private static final DerivedProperty ORIGINS = new DerivedProperty("publishLocation", "hasPublishLocation", "getIdentificationName", "getOrigin");
  private static final List<DerivedProperty> DERIVED_PROPERTIES = ImmutableList.of(AUTHOR_NAME, AUTHOR_GENDER, GENRES, LIBRARIES, ORIGINS, AUTHOR_BIRTH_DATE, AUTHOR_DEATH_DATE, AUTHOR_CHILDREN, AUTHOR_TYPE);

  @Override
  public List<DerivedProperty> getDerivedProperties() {
    return ImmutableList.<DerivedProperty>builder().addAll(DERIVED_PROPERTIES).addAll(super.getDerivedProperties()).build();
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_gender", canBeEmpty = true, isFaceted = true)
  public Object getAuthorGender() {
    return this.getProperty(AUTHOR_GENDER.getPropertyName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_t_author_name", canBeEmpty = true)
  public Object getAuthorName() {
    return this.getProperty(AUTHOR_NAME.getPropertyName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_i_author_birthDate", canBeEmpty = true, isFaceted = true, facetType = FacetType.RANGE)
  public Datable getAuthorBirthDate() {
    return (Datable) this.getProperty(AUTHOR_BIRTH_DATE.getPropertyName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_i_author_deathDate", canBeEmpty = true, isFaceted = true, facetType = FacetType.RANGE)
  public Datable getAuthordeathDate() {
    return (Datable) this.getProperty(AUTHOR_DEATH_DATE.getPropertyName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_children", canBeEmpty = true, isFaceted = true)
  public Object getAuthorChildren() {
    return this.getProperty(AUTHOR_CHILDREN.getPropertyName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_types", canBeEmpty = true, isFaceted = true)
  public Object getAuthorType() {
    return this.getProperty(AUTHOR_TYPE.getPropertyName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_genre", canBeEmpty = true, isFaceted = true)
  public Object getGenres() {
    return this.getProperty(GENRES.getPropertyName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_library", canBeEmpty = true, isFaceted = false)
  public Object getLibraries() {
    // Relation with collectives with type "library".
    return getProperty(LIBRARIES.getPropertyName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_origin", canBeEmpty = true, isFaceted = true)
  public Object getOrigin() {
    return getProperty(ORIGINS.getPropertyName());
  }

  private static final DerivedRelationDescription AUTHOR_RESIDENCE = new DerivedRelationDescription("hasAuthorResidence", "isCreatedBy", "hasResidenceLocation");
  private static final DerivedRelationDescription AUTHOR_BIRTHPLACE = new DerivedRelationDescription("hasAuthorBirthPlace", "isCreatedBy", "hasBirthPlace");
  private static final DerivedRelationDescription AUTHOR_DEATHPLACE = new DerivedRelationDescription("hasAuthorDeathPlace", "isCreatedBy", "hasDeathPlace");
  private static final DerivedRelationDescription AUTHOR_RELIGION = new DerivedRelationDescription("hasAuthorReligion", "isCreatedBy", "hasReligion");
  private static final DerivedRelationDescription AUTHOR_MEMBERSHIPS = new DerivedRelationDescription("hasAuthorMemberships", "isCreatedBy", "isMemberOf");
  private static final DerivedRelationDescription AUTHOR_MARITAL_STATUS = new DerivedRelationDescription("hasAuthorMaritalStatus", "isCreatedBy", "hasMaritalStatus");
  private static final DerivedRelationDescription AUTHOR_SOCIAL_CLASS = new DerivedRelationDescription("hasAuthorSocialClass", "isCreatedBy", "hasSocialClass");
  private static final DerivedRelationDescription AUTHOR_EDUCATION = new DerivedRelationDescription("hasAuthorEducation", "isCreatedBy", "hasEducation");
  private static final DerivedRelationDescription AUTHOR_PROFESSION = new DerivedRelationDescription("hasAuthorProfession", "isCreatedBy", "hasProfession");
  private static final DerivedRelationDescription AUTHOR_FINANCIALS = new DerivedRelationDescription("hasAuthorFinancialSituation", "isCreatedBy", "hasFinancialSituation");
  private static final List<DerivedRelationDescription> DERIVED_RELATION_TYPES = ImmutableList.of(AUTHOR_BIRTHPLACE, AUTHOR_DEATHPLACE, AUTHOR_MEMBERSHIPS, AUTHOR_RELIGION, AUTHOR_RESIDENCE, AUTHOR_MARITAL_STATUS, AUTHOR_EDUCATION, AUTHOR_SOCIAL_CLASS, AUTHOR_FINANCIALS, AUTHOR_PROFESSION);

  @Override
  public List<DerivedRelationDescription> getDerivedRelationDescriptions() {
    return DERIVED_RELATION_TYPES;
  }


  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_residence", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getAuthorResidence() {
    return getRelations(AUTHOR_RESIDENCE.getDerivedTypeName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_birthplace", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getAuthorBirthPlace() {
    return getRelations(AUTHOR_BIRTHPLACE.getDerivedTypeName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_deathplace", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getAuthorDeathPlace() {
    return getRelations(AUTHOR_DEATHPLACE.getDerivedTypeName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_religion", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getAuthorReligion() {
    return getRelations(AUTHOR_RELIGION.getDerivedTypeName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_collective", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getAuthorMembership() {
    return getRelations(AUTHOR_MEMBERSHIPS.getDerivedTypeName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_social_class", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getAuthorSocialClass() {
    return getRelations(AUTHOR_SOCIAL_CLASS.getDerivedTypeName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_marital_status", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getAuthorMaritalStatus() {
    return getRelations(AUTHOR_MARITAL_STATUS.getDerivedTypeName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_education", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getAuthorEducation() {
    return getRelations(AUTHOR_EDUCATION.getDerivedTypeName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_profession", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getAuthorProfession() {
    return getRelations(AUTHOR_PROFESSION.getDerivedTypeName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_financials", accessors = {"getDisplayName"}, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getAuthorFinancials() {
    return getRelations(AUTHOR_FINANCIALS.getDerivedTypeName());
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_author_relatedLocations", canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getAuthorRelatedLocations() {
    List<RelationRef> relatedLocations = Lists.newArrayList();

    List<RelationRef> residenceLocations = this.getAuthorResidence();
    if (residenceLocations != null) {
      relatedLocations.addAll(residenceLocations);
    }

    List<RelationRef> birthPlaces = getAuthorBirthPlace();
    if (birthPlaces != null) {
      relatedLocations.addAll(birthPlaces);
    }

    List<RelationRef> deathPlaces = getAuthorDeathPlace();
    if (deathPlaces != null) {
      relatedLocations.addAll(deathPlaces);
    }

    return relatedLocations;
  }


  @Override
  public Map<String, String> getClientRepresentation() {
    Map<String, String> data = Maps.newTreeMap();
    addItemToRepresentation(data, "id", getId());
    addItemToRepresentation(data, "title", getTitle());
    addItemToRepresentation(data, "type", getDocumentType());
    addItemToRepresentation(data, "date", getDate() != null ? getDate().getFromYear() : null);
    addRelationToRepresentation(data, "genre", "hasGenre");
    addRelationToRepresentation(data, "language", "hasWorkLanguage");
    addRelationToRepresentation(data, "publishLocation", "hasPublishLocation");
    addRelationToRepresentation(data, "createdBy", "isCreatedBy");
    addItemToRepresentation(data, "authorGender", getProperty("authorGender"));
    return data;
  }

  @Override
  public <T> Map<String, T> createRelSearchRep(Map<String, T> mappedIndexInformation) {
    Map<String, T> filteredMap = Maps.newTreeMap();
    addValueToMap(mappedIndexInformation, filteredMap, ID_PROPERTY_NAME);
    addValueToMap(mappedIndexInformation, filteredMap, "title");
    addValueToMap(mappedIndexInformation, filteredMap, "documentType");
    addValueToMap(mappedIndexInformation, filteredMap, "date");
    addValueToMap(mappedIndexInformation, filteredMap, "genre");
    addValueToMap(mappedIndexInformation, filteredMap, "language");
    addValueToMap(mappedIndexInformation, filteredMap, "publishLocation");
    addValueToMap(mappedIndexInformation, filteredMap, "authorName");
    addValueToMap(mappedIndexInformation, filteredMap, "authorGender");
    return filteredMap;
  }

  // ---------------------------------------------------------------------------
  @JsonIgnore
  @OAIDublinCoreField(dublinCoreField = DublinCoreMetadataField.TITLE)
  public String getCMDITitle() {
    return getTitle();
  }

  @JsonIgnore
  @OAIDublinCoreField(dublinCoreField = DublinCoreMetadataField.DESCRIPTION)
  public String getCMDIDescription() {
    StringBuilder sb = new StringBuilder();

    List<RelationRef> origins = getOrigins();
    if (origins != null) {
      for (RelationRef ref : origins) {
        Text.appendTo(sb, ref.getDisplayName(), " ");
      }
    }

    Text.appendTo(sb, getDocumentType(), " ");

    return sb.toString();
  }

}
