package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.IDPrefix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A relation between domain documents.
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
@IDPrefix("REL")
public class Relation extends DomainDocument {

  /** A reference to the 'active' participant of the relation (resembles rdf:subject). */
  private String sourceRefType;
  private String sourceRefId;
  /** A reference to the property or characteristic of the subject (resembles rdf:predicate). */
  private String typeRefType;
  private String typeRefId;
  /** A reference to the 'passive' participant of the relation (resembles rdf:object). */
  private String targetRefType;
  private String targetRefId;

  // For deserialization...
  public Relation() {}

  public Relation(Reference sourceRef, Reference typeRef, Reference targetRef) {
    // TODO validate consistency
    setSourceRef(sourceRef);
    setTypeRef(typeRef);
    setTargetRef(targetRef);
  }

  @Override
  public String getDisplayName() {
    return String.format("({%s,%s}, {%s,%s}, {%s,%s})", sourceRefType, sourceRefId, typeRefType, typeRefId, targetRefType, targetRefId);
  }

  @JsonIgnore
  public Reference getSourceRef() {
    Reference reference = new Reference();
    reference.setType(sourceRefType);
    reference.setId(sourceRefId);
    return reference;
  }

  @JsonIgnore
  public void setSourceRef(Reference reference) {
    sourceRefType = reference.getType();
    sourceRefId = reference.getId();
  }

  @JsonProperty("^sourceRefType")
  public String getSourceRefType() {
    return sourceRefType;
  }

  @JsonProperty("^sourceRefType")
  public void setSourceRefType(String sourceRefType) {
    this.sourceRefType = sourceRefType;
  }

  @JsonProperty("^sourceRefId")
  public String getSourceRefId() {
    return sourceRefId;
  }

  @JsonProperty("^sourceRefId")
  public void setSourceRefId(String sourceRefId) {
    this.sourceRefId = sourceRefId;
  }

  @JsonIgnore
  public Reference getTypeRef() {
    Reference reference = new Reference();
    reference.setType(typeRefType);
    reference.setId(typeRefId);
    return reference;
  }

  @JsonIgnore
  public void setTypeRef(Reference reference) {
    typeRefType = reference.getType();
    typeRefId = reference.getId();
  }

  @JsonProperty("^typeRefType")
  public String getTypeRefType() {
    return typeRefType;
  }

  @JsonProperty("^typeRefType")
  public void setTypeRefType(String typeRefType) {
    this.typeRefType = typeRefType;
  }

  @JsonProperty("^typeRefId")
  public String getTypeRefId() {
    return typeRefId;
  }

  @JsonProperty("^typeRefId")
  public void setTypeRefId(String typeRefId) {
    this.typeRefId = typeRefId;
  }

  @JsonIgnore
  public Reference getTargetRef() {
    Reference reference = new Reference();
    reference.setType(targetRefType);
    reference.setId(targetRefId);
    return reference;
  }

  @JsonIgnore
  public void setTargetRef(Reference reference) {
    targetRefType = reference.getType();
    targetRefId = reference.getId();
  }

  @JsonProperty("^targetRefType")
  public String getTargetRefType() {
    return targetRefType;
  }

  @JsonProperty("^targetRefType")
  public void setTargetRefType(String targetRefType) {
    this.targetRefType = targetRefType;
  }

  @JsonProperty("^targetRefId")
  public String getTargetRefId() {
    return targetRefId;
  }

  @JsonProperty("^targetRefId")
  public void setTargetRefId(String targetRefId) {
    this.targetRefId = targetRefId;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Relation) {
      Relation that = (Relation) object;
      return (this.typeRefType == null ? that.typeRefType == null : this.typeRefType.equals(that.typeRefType)) //
          && (this.typeRefId == null ? that.typeRefId == null : this.typeRefId.equals(that.typeRefId)) //
          && (this.sourceRefType == null ? that.sourceRefType == null : this.sourceRefType.equals(that.sourceRefType)) //
          && (this.sourceRefId == null ? that.sourceRefId == null : this.sourceRefId.equals(that.sourceRefId)) //
          && (this.targetRefType == null ? that.targetRefType == null : this.targetRefType.equals(that.targetRefType)) //
          && (this.targetRefId == null ? that.targetRefId == null : this.targetRefId.equals(that.targetRefId));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (typeRefType == null ? 0 : typeRefType.hashCode());
    result = 31 * result + (typeRefId == null ? 0 : typeRefId.hashCode());
    result = 31 * result + (sourceRefType == null ? 0 : sourceRefType.hashCode());
    result = 31 * result + (sourceRefId == null ? 0 : sourceRefId.hashCode());
    result = 31 * result + (targetRefType == null ? 0 : targetRefType.hashCode());
    result = 31 * result + (targetRefId == null ? 0 : targetRefId.hashCode());
    return result;
  }

}
