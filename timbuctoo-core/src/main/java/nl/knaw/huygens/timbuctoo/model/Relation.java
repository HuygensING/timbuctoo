package nl.knaw.huygens.timbuctoo.model;

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

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.validation.RelationDuplicationValidator;
import nl.knaw.huygens.timbuctoo.validation.RelationReferenceValidator;
import nl.knaw.huygens.timbuctoo.validation.RelationTypeConformationValidator;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * A relation between domain entities.
 *
 * (This resembles an RDF statement.)
 * 
 * N.B. Relations are indexed by {@code RelationIndexer),
 * which does not require index annotations.
 * 
 * There is a conceptual problem to solve.
 * Suppose we have a relation between an ATLArchive and an ATLArchiver.
 * What types do we use? Those types or the primitive types?
 * When indexing, this problem comes back to use straightaway:
 * If we want to store the displayname (which we do), it depends on
 * the type specified which value we retrieve from the database
 * (it's a different variation we retrieve), so "just" indexing is
 * too simple a concept: we need an index for a VRE.
 */
@IDPrefix("RELA")
public class Relation extends DomainEntity {

  /** A reference to the 'active' participant of the relation (resembles rdf:subject). */
  private String sourceType;
  private String sourceId;
  /** A reference to the property or characteristic of the subject (resembles rdf:predicate). */
  private String typeType;
  private String typeId;
  /** A reference to the 'passive' participant of the relation (resembles rdf:object). */
  private String targetType;
  private String targetId;
  /**
   * Do we accept the existence of this relation? As such it also controls
   * the visibility of this relation for VRE's.
   */
  private boolean accepted = true;

  // For deserialization...
  public Relation() {}

  public Relation(Reference sourceRef, Reference typeRef, Reference targetRef) {
    setSourceRef(sourceRef);
    setTypeRef(typeRef);
    setTargetRef(targetRef);
  }

  @Override
  public String getDisplayName() {
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

  @JsonProperty("^sourceType")
  public String getSourceType() {
    return sourceType;
  }

  @JsonProperty("^sourceType")
  public void setSourceType(String sourceRefType) {
    this.sourceType = sourceRefType;
  }

  @JsonProperty("^sourceId")
  public String getSourceId() {
    return sourceId;
  }

  public boolean hasSourceId(String id) {
    return sourceId != null && sourceId.equals(id);
  }

  @JsonProperty("^sourceId")
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

  @JsonProperty("^typeType")
  public String getTypeType() {
    return typeType;
  }

  @JsonProperty("^typeType")
  public void setTypeType(String typeRefType) {
    this.typeType = typeRefType;
  }

  @JsonProperty("^typeId")
  public String getTypeId() {
    return typeId;
  }

  @JsonProperty("^typeId")
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

  @JsonProperty("^targetType")
  public String getTargetType() {
    return targetType;
  }

  @JsonProperty("^targetType")
  public void setTargetType(String targetRefType) {
    this.targetType = targetRefType;
  }

  @JsonProperty("^targetId")
  public String getTargetId() {
    return targetId;
  }

  public boolean hasTargetId(String id) {
    return targetId != null && targetId.equals(id);
  }

  @JsonProperty("^targetId")
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
  public void validateForAdd(TypeRegistry registry, StorageManager storage) throws ValidationException {
    super.validateForAdd(registry, storage);
    new RelationTypeConformationValidator(storage).validate(this);
    new RelationReferenceValidator(registry, storage).validate(this);
    new RelationDuplicationValidator(storage).validate(this);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Relation) {
      Relation that = (Relation) object;
      return (this.typeType == null ? that.typeType == null : this.typeType.equals(that.typeType)) //
          && (this.typeId == null ? that.typeId == null : this.typeId.equals(that.typeId)) //
          && (this.sourceType == null ? that.sourceType == null : this.sourceType.equals(that.sourceType)) //
          && (this.sourceId == null ? that.sourceId == null : this.sourceId.equals(that.sourceId)) //
          && (this.targetType == null ? that.targetType == null : this.targetType.equals(that.targetType)) //
          && (this.targetId == null ? that.targetId == null : this.targetId.equals(that.targetId));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (typeType == null ? 0 : typeType.hashCode());
    result = 31 * result + (typeId == null ? 0 : typeId.hashCode());
    result = 31 * result + (sourceType == null ? 0 : sourceType.hashCode());
    result = 31 * result + (sourceId == null ? 0 : sourceId.hashCode());
    result = 31 * result + (targetType == null ? 0 : targetType.hashCode());
    result = 31 * result + (targetId == null ? 0 : targetId.hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Relation{\ntypeType: ");
    sb.append(typeType);
    sb.append("\ntypeId: ");
    sb.append(typeId);
    sb.append("\nsourceType: ");
    sb.append(sourceType);
    sb.append("\nsourceId: ");
    sb.append(sourceId);
    sb.append("\ntargetType: ");
    sb.append(targetType);
    sb.append("\ntargetId: ");
    sb.append(targetId);

    return sb.toString();
  }

  public boolean conformsToRelationType(RelationType relationType) {
    return Objects.equal(relationType.getSourceTypeName(), sourceType) //
        && Objects.equal(relationType.getTargetTypeName(), targetType);
  }

  public boolean hasSource() {
    return !StringUtils.isBlank(sourceId);
  }

  public boolean hasTarget() {
    return !StringUtils.isBlank(targetId);
  }

}
