package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo core
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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.annotations.DBProperty;
import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.validation.RelationValidator;

/**
 * A relation between primitive domain entities.
 */
@IDPrefix("RELA")
public class Relation extends DomainEntity {

  public static final String TARGET_TYPE = "^targetType";
  public static final String TYPE_TYPE = "^typeType";
  public static final String SOURCE_TYPE = "^sourceType";
  public static final String SOURCE_ID = "^sourceId";
  public static final String TARGET_ID = "^targetId";
  public static final String TYPE_ID = "^typeId";

  public static final String SOURCE_ID_FACET_NAME = "dynamic_s_sourceId";
  public static final String TARGET_ID_FACET_NAME = "dynamic_s_targetId";
  public static final String TYPE_ID_FACET_NAME = "dynamic_s_typeId";
  public static final String DB_TYPE_ID_PROP_NAME = "typeId";
  public static final String INDEX_FIELD_TARGET_TYPE = "dynamic_s_targetType";
  public static final String INDEX_FIELD_SOURCE_TYPE = "dynamic_s_sourceType";

  /**
   * A reference to the 'active' participant of the relation (resembles rdf:subject).
   */
  @DBProperty(value = "sourceType", type = FieldType.VIRTUAL)
  private String sourceType;
  @DBProperty(value = "sourceId", type = FieldType.VIRTUAL)
  private String sourceId;
  /**
   * A reference to the property or characteristic of the subject (resembles rdf:predicate).
   */
  @DBProperty(value = "typeType", type = FieldType.ADMINISTRATIVE)
  private String typeType = "relationtype";
  @DBProperty(value = DB_TYPE_ID_PROP_NAME, type = FieldType.ADMINISTRATIVE)
  private String typeId;
  /**
   * A reference to the 'passive' participant of the relation (resembles rdf:object).
   */
  @DBProperty(value = "targetType", type = FieldType.VIRTUAL)
  private String targetType;
  @DBProperty(value = "targetId", type = FieldType.VIRTUAL)
  private String targetId;
  /**
   * Do we accept the existence of this relation? As such it also controls
   * the visibility of this relation for VRE's.
   */
  private boolean accepted;

  public Relation() {
    setAccepted(true);
  }

  @Override
  public String getIdentificationName() {
    return String.format("(%s: {%s,%s} --> {%s,%s})", typeId, sourceType, sourceId, targetType, targetId);
  }

  @JsonIgnore
  public Reference getSourceRef() {
    Reference reference = new Reference();
    reference.setType(sourceType);
    reference.setId(sourceId);
    return reference;
  }

  @JsonIgnore
  public void setSourceRef(Reference reference) {
    sourceType = reference.getType();
    sourceId = reference.getId();
  }

  @JsonProperty(SOURCE_TYPE)
  @IndexAnnotation(fieldName = INDEX_FIELD_SOURCE_TYPE)
  public String getSourceType() {
    return sourceType;
  }

  @JsonProperty(SOURCE_TYPE)
  public void setSourceType(String sourceRefType) {
    this.sourceType = sourceRefType;
  }

  @JsonProperty(SOURCE_ID)
  @IndexAnnotation(fieldName = SOURCE_ID_FACET_NAME, isFaceted = true)
  public String getSourceId() {
    return sourceId;
  }

  public boolean hasSourceId(String id) {
    return sourceId != null && sourceId.equals(id);
  }

  @JsonProperty(SOURCE_ID)
  public void setSourceId(String sourceRefId) {
    this.sourceId = sourceRefId;
  }

  @JsonIgnore
  public Reference getTypeRef() {
    Reference reference = new Reference();
    reference.setType(typeType);
    reference.setId(typeId);
    return reference;
  }

  @JsonIgnore
  public void setTypeRef(Reference reference) {
    typeType = reference.getType();
    typeId = reference.getId();
  }

  @JsonProperty(TYPE_TYPE)
  public String getTypeType() {
    return typeType;
  }

  @JsonProperty(TYPE_TYPE)
  public void setTypeType(String typeRefType) {
    this.typeType = typeRefType;
  }

  @JsonProperty(TYPE_ID)
  @IndexAnnotation(fieldName = TYPE_ID_FACET_NAME, isFaceted = true)
  public String getTypeId() {
    return typeId;
  }

  @JsonProperty(TYPE_ID)
  public void setTypeId(String typeRefId) {
    this.typeId = typeRefId;
  }

  @JsonIgnore
  public Reference getTargetRef() {
    Reference reference = new Reference();
    reference.setType(targetType);
    reference.setId(targetId);
    return reference;
  }

  @JsonIgnore
  public void setTargetRef(Reference reference) {
    targetType = reference.getType();
    targetId = reference.getId();
  }

  @JsonProperty(TARGET_TYPE)
  @IndexAnnotation(fieldName = INDEX_FIELD_TARGET_TYPE)
  public String getTargetType() {
    return targetType;
  }

  @JsonProperty(TARGET_TYPE)
  public void setTargetType(String targetRefType) {
    this.targetType = targetRefType;
  }

  @JsonProperty(TARGET_ID)
  @IndexAnnotation(fieldName = TARGET_ID_FACET_NAME, isFaceted = true)
  public String getTargetId() {
    return targetId;
  }

  public boolean hasTargetId(String id) {
    return targetId != null && targetId.equals(id);
  }

  @JsonProperty(TARGET_ID)
  public void setTargetId(String targetRefId) {
    this.targetId = targetRefId;
  }

  public boolean isAccepted() {
    return accepted;
  }

  public void setAccepted(boolean accepted) {
    this.accepted = accepted;
  }

  @Override
  public void normalize(Repository repository) {
    // Make sure symmetric relations are stored in canonical order
    if (typeId != null & sourceId != null && targetId != null) {
      RelationType relationType = repository.getRelationTypeById(typeId, false);
      if (relationType != null && relationType.isSymmetric() && sourceId.compareTo(targetId) > 0) {
        String temp = sourceId;
        sourceId = targetId;
        targetId = temp;
      }
    }
  }

  @Override
  public void validateForAdd(Repository storage) throws ValidationException {
    super.validateForAdd(storage);
    new RelationValidator(storage).validate(this);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Relation) {
      Relation that = (Relation) object;
      return Objects.equal(this.typeType, that.typeType) //
        && Objects.equal(this.typeId, that.typeId) //
        && Objects.equal(this.sourceType, that.sourceType) //
        && Objects.equal(this.sourceId, that.sourceId) //
        && Objects.equal(this.targetType, that.targetType) //
        && Objects.equal(this.targetId, that.targetId);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(typeType, typeId, sourceType, sourceId, targetType, targetId);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this) //
      .add("id", getId()) //
      .add("typeId", typeId) //
      .add("sourceType", sourceType) //
      .add("sourceId", sourceId) //
      .add("targetType", targetType) //
      .add("targetId", targetId) //
      .toString();
  }

}
